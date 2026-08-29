package ru.wolfram.feed.s3

import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.config.common.annotation.ConfigSource
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@ConfigSource("s3")
interface S3Config {
    fun publicEndpoint(): String

    fun region(): String

    fun bucket(): String

    fun accessKey(): String

    fun secretKey(): String

    fun urlExpirationMinutes(): Long
}

@Module
interface S3Module {
    fun s3Presigner(config: S3Config): S3Presigner {
        return S3Presigner.builder()
            .endpointOverride(URI.create(config.publicEndpoint()))
            .region(Region.of(config.region()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        config.accessKey(),
                        config.secretKey()
                    )
                )
            )
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()
    }
}