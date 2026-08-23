package ru.wolfram.postcreator.controller

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.Header
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.form.FormMultipart.FormPart
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.JsonWriter
import ru.wolfram.postcreator.dto.CreatePostResponse
import ru.wolfram.postcreator.service.ImageData
import ru.wolfram.postcreator.service.PostService
import java.nio.charset.StandardCharsets

@Component
@HttpController
class PostController(
    private val postService: PostService,
    private val responseJsonWriter: JsonWriter<CreatePostResponse>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @HttpRoute(method = HttpMethod.POST, path = "/v1/posts")
    suspend fun createPost(
        @Header("X-User-Id") userId: Long,
        body: FormMultipart
    ): HttpServerResponse {
        logger.info("Received ${body.parts().size} parts")
        body.parts().forEachIndexed { i, part ->
            logger.info("  Part $i: name='${part.name()}', type=${part.javaClass.simpleName}")
        }
        val text = extractText(body)
        val images = extractImages(body)

        val response = postService.createPost(userId, text, images)
        return HttpServerResponse.of(
            200,
            HttpBody.json(responseJsonWriter.toString(response))
        )
    }

    private fun extractText(body: FormMultipart): String {
        val textPart = body.parts()
            .firstOrNull { it.name() == "text" }
            ?: throw HttpServerResponseException.of(400, "Field 'text' is required")

        val value = when (textPart) {
            is FormPart.MultipartData -> textPart.content()
            is FormPart.MultipartFile -> textPart.content().toString(StandardCharsets.UTF_8)
            is FormPart.MultipartFileStream ->
                throw HttpServerResponseException.of(400, "Text field cannot be a stream")
        }

        if (value.isBlank()) {
            throw HttpServerResponseException.of(400, "Field 'text' cannot be empty")
        }

        return value
    }

    private fun extractImages(body: FormMultipart): List<ImageData> {
        return body.parts()
            .filterIsInstance<FormPart.MultipartFile>()
            .filter { it.name() == "images" }
            .map { file ->
                ImageData(
                    bytes = file.content(),
                    fileName = file.fileName() ?: "unnamed.jpg"
                )
            }
    }
}