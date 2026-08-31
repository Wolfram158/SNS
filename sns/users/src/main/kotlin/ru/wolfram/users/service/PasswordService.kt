package ru.wolfram.users.service

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Component
class PasswordService {
    private val logger = LoggerFactory.getLogger(PasswordService::class.java)
    private val secureRandom = SecureRandom()

    companion object {
        private const val MEMORY_KB = 65536
        private const val ITERATIONS = 3
        private const val PARALLELISM = 1
        private const val HASH_LENGTH = 32
        private const val SALT_LENGTH = 16

        private const val ARGON2_VERSION = Argon2Parameters.ARGON2_VERSION_13
        private const val ARGON2_TYPE = Argon2Parameters.ARGON2_id

        private const val FORMAT_PREFIX = "argon2id"
        private const val FORMAT_VERSION = 19

        private val BASE64_ENCODER = Base64.getEncoder().withoutPadding()
        private val BASE64_DECODER = Base64.getDecoder()
    }

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)

        val params = Argon2Parameters.Builder(ARGON2_TYPE)
            .withVersion(ARGON2_VERSION)
            .withMemoryAsKB(MEMORY_KB)
            .withIterations(ITERATIONS)
            .withParallelism(PARALLELISM)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)

        val hash = ByteArray(HASH_LENGTH)
        generator.generateBytes(password.toCharArray(), hash)

        return formatHash(salt, hash)
    }

    fun verify(password: String, encodedHash: String): Boolean {
        val parsed = parseHash(encodedHash)
        if (parsed == null) {
            logger.warn("Failed to parse Argon2 hash")
            return false
        }

        val params = Argon2Parameters.Builder(ARGON2_TYPE)
            .withVersion(ARGON2_VERSION)
            .withMemoryAsKB(parsed.memory)
            .withIterations(parsed.iterations)
            .withParallelism(parsed.parallelism)
            .withSalt(parsed.salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)

        val computedHash = ByteArray(parsed.hash.size)
        generator.generateBytes(password.toCharArray(), computedHash)

        return MessageDigest.isEqual(computedHash, parsed.hash)
    }

    private fun formatHash(salt: ByteArray, hash: ByteArray): String {
        val saltB64 = BASE64_ENCODER.encodeToString(salt)
        val hashB64 = BASE64_ENCODER.encodeToString(hash)

        return "$$FORMAT_PREFIX\$v=$FORMAT_VERSION$" +
                "m=$MEMORY_KB,t=$ITERATIONS,p=$PARALLELISM$$saltB64$$hashB64"
    }

    private fun parseHash(encoded: String): ParsedHash? {
        val parts = encoded.split("$").filter { it.isNotEmpty() }
        if (parts.size != 5) {
            return null
        }

        if (parts[0] != FORMAT_PREFIX) {
            return null
        }

        if (parts[1] != "v=$FORMAT_VERSION") {
            return null
        }

        val params = parts[2]
            .split(",")
            .associate {
                val keyValue = it.split("=", limit = 2)
                if (keyValue.size != 2) return null
                keyValue[0] to (keyValue[1].toIntOrNull() ?: return null)
            }

        val memory = params["m"] ?: return null
        val iterations = params["t"] ?: return null
        val parallelism = params["p"] ?: return null

        return try {
            ParsedHash(
                memory = memory,
                iterations = iterations,
                parallelism = parallelism,
                salt = BASE64_DECODER.decode(parts[3]),
                hash = BASE64_DECODER.decode(parts[4])
            )
        } catch (e: Exception) {
            logger.warn("Failed to decode Argon2 hash", e)
            null
        }
    }

    private class ParsedHash(
        val memory: Int,
        val iterations: Int,
        val parallelism: Int,
        val salt: ByteArray,
        val hash: ByteArray
    )
}