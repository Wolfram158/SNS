package ru.wolfram.feed.s3

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration

@Component
class S3Service(
    private val presigner: S3Presigner,
    private val s3Config: S3Config
) {
    private val logger = LoggerFactory.getLogger(S3Service::class.java)

    fun generatePresignedGetUrl(key: String): String {
        val request = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(s3Config.urlExpirationMinutes()))
            .getObjectRequest { req ->
                req.bucket(s3Config.bucket()).key(key)
            }
            .build()

        val url = presigner.presignGetObject(request).url().toString()
        logger.debug("Generated presigned URL for key={}", key)
        return url
    }

    fun generatePresignedGetUrls(keys: List<String>): List<String> {
        return keys.map { generatePresignedGetUrl(it) }
    }
}