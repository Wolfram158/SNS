package ru.wolfram.postcreator.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.database.r2dbc.R2dbcConnectionFactory
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.json.common.JsonWriter
import ru.wolfram.postcreator.dao.OutboxEventDAO
import ru.wolfram.postcreator.dao.PostDAO
import ru.wolfram.postcreator.dao.PostImageDAO
import ru.wolfram.postcreator.dto.CreatePostResponse
import ru.wolfram.postcreator.dto.PostCreatedEvent
import ru.wolfram.postcreator.repository.OutboxRepository
import ru.wolfram.postcreator.repository.PostRepository
import java.time.OffsetDateTime
import java.util.*

@Component
class PostService(
    private val postRepository: PostRepository,
    private val outboxRepository: OutboxRepository,
    private val s3Storage: S3StorageService,
    private val connectionFactory: R2dbcConnectionFactory,
    private val postCreatedJsonWriter: JsonWriter<PostCreatedEvent>
) {
    companion object {
        const val MAX_IMAGES = 10
        const val MAX_TEXT_LENGTH = 3000
        const val MAX_IMAGE_SIZE = 10 * 1024 * 1024
    }

    suspend fun createPost(authorId: Long, text: String, images: List<ImageData>): CreatePostResponse {
        validate(text, images)

        val postId = UUID.randomUUID()
        val createdAt = OffsetDateTime.now()

        val s3Keys = uploadImagesToS3(images)

        try {
            savePostInTransaction(postId, authorId, text, createdAt, s3Keys)
        } catch (e: Exception) {
            compensateS3Uploads(s3Keys)
            throw e
        }

        return CreatePostResponse(
            id = postId,
            authorId = authorId,
            text = text,
            createdAt = createdAt,
            imageKeys = s3Keys.map { it.key }
        )
    }

    private suspend fun uploadImagesToS3(images: List<ImageData>): List<S3KeyInfo> {
        return images.mapIndexed { index, image ->
            val key = s3Storage.uploadImage(image.bytes, image.fileName)
            S3KeyInfo(index, key, image)
        }
    }

    private suspend fun compensateS3Uploads(s3Keys: List<S3KeyInfo>) {
        s3Keys.forEach { s3KeyInfo ->
            try {
                s3Storage.deleteImage(s3KeyInfo.key)
            } catch (e: Exception) {
                System.err.println("Failed to delete S3 object ${s3KeyInfo.key}: ${e.message}")
            }
        }
    }

    private suspend fun savePostInTransaction(
        postId: UUID,
        authorId: Long,
        text: String,
        createdAt: OffsetDateTime,
        s3Keys: List<S3KeyInfo>
    ) {
        connectionFactory.inTx {
            mono {
                postRepository.insert(
                    PostDAO(
                        id = postId,
                        authorId = authorId,
                        text = text,
                        createdAt = createdAt
                    )
                )

                s3Keys.forEach { s3KeyInfo ->
                    postRepository.insertImage(
                        PostImageDAO(
                            id = UUID.randomUUID(),
                            postId = postId,
                            s3Key = s3KeyInfo.key,
                            position = s3KeyInfo.position
                        )
                    )
                }

                val event = PostCreatedEvent(
                    postId = postId,
                    authorId = authorId,
                    text = text,
                    createdAt = createdAt,
                    imageKeys = s3Keys.map { it.key }
                )

                outboxRepository.insert(
                    OutboxEventDAO(
                        id = UUID.randomUUID(),
                        aggregateType = "Post",
                        aggregateId = postId,
                        eventType = "PostCreated",
                        payload = postCreatedJsonWriter.toString(event),
                        createdAt = createdAt
                    )
                )
            }
        }.awaitSingleOrNull()
    }

    private fun validate(text: String, images: List<ImageData>) {
        if (text.isBlank()) {
            throw HttpServerResponseException.of(400, "Post text cannot be empty")
        }
        if (text.length > MAX_TEXT_LENGTH) {
            throw HttpServerResponseException.of(400, "Text too long (max $MAX_TEXT_LENGTH)")
        }
        if (images.size > MAX_IMAGES) {
            throw HttpServerResponseException.of(400, "Too many images (max $MAX_IMAGES)")
        }
        images.forEachIndexed { index, image ->
            if (image.bytes.size > MAX_IMAGE_SIZE) {
                throw HttpServerResponseException.of(
                    400,
                    "Image ${index + 1} too large (max 10MB)"
                )
            }
        }
    }

    private data class S3KeyInfo(
        val position: Int,
        val key: String,
        val imageData: ImageData
    )
}

class ImageData(
    val bytes: ByteArray,
    val fileName: String
)