package ru.wolfram.postcreator.service

import kotlinx.coroutines.future.await
import ru.tinkoff.kora.common.Component
import ru.wolfram.postcreator.s3.S3ClientHolder
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Component
class S3StorageService(
    private val s3Holder: S3ClientHolder
) {
    suspend fun uploadImage(bytes: ByteArray, fileName: String): String {
        val extension = fileName.substringAfterLast('.', "jpg")
        val s3Key = "posts/${UUID.randomUUID()}.${extension}"

        val request = PutObjectRequest.builder()
            .bucket(s3Holder.bucket)
            .key(s3Key)
            .contentType("image/$extension")
            .build()

        s3Holder.client.putObject(request, AsyncRequestBody.fromBytes(bytes)).await()

        return s3Key
    }

    suspend fun deleteImage(s3Key: String) {
        val request = DeleteObjectRequest.builder()
            .bucket(s3Holder.bucket)
            .key(s3Key)
            .build()

        s3Holder.client.deleteObject(request).await()
    }
}