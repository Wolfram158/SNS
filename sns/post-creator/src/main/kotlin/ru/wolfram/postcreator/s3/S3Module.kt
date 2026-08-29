package ru.wolfram.postcreator.s3

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@ConfigSource("s3")
interface S3Config {
    fun endpoint(): String
    fun region(): String
    fun bucket(): String
    fun accessKey(): String
    fun secretKey(): String
}

@Component
@Root
class S3ClientHolder(config: S3Config) {
    val client: S3AsyncClient = S3AsyncClient.builder()
        .endpointOverride(URI.create(config.endpoint()))
        .region(Region.of(config.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.accessKey(), config.secretKey())
            )
        )
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build()
        )
        .build()

    val bucket: String = config.bucket()
}