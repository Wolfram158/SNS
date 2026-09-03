package ru.wolfram.subscribers.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.guide.testingintegration.ru.wolfram.subscribers.TestApplication
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import ru.wolfram.subscribers.dto.SubscribeRequest
import java.time.Duration

@Testcontainers
@KoraAppTest(TestApplication::class)
class UserServiceIntegrationPostgresTest : KoraAppTestConfigModifier {
    companion object {
        @Container
        @JvmStatic
        val POSTGRES = PostgreSQLContainer("postgres:17-alpine")
            .withStartupTimeout(Duration.ofSeconds(30))
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer::class.java)))
    }

    @TestComponent
    lateinit var subscriptionService: SubscriptionService

    @TestComponent
    lateinit var testSubscriptionRepository: TestApplication.TestSubscriptionRepository

    override fun config(): KoraConfigModification {
        val jdbcUrl = POSTGRES.jdbcUrl
        val r2dbcUrl = jdbcUrl.replace("jdbc:", "r2dbc:")
        return KoraConfigModification.ofString(
            """
            db {
              jdbcUrl = "${'$'}{POSTGRES_JDBC_URL}"
              r2dbcUrl = "${'$'}{POSTGRES_R2DBC_URL}"
              username = "${'$'}{POSTGRES_USER}"
              password = "${'$'}{POSTGRES_PASS}"
              poolName = "kora-test"
            }
            flyway {
              locations = ["db/migration"]
            }
            """.trimIndent()
        )
            .withSystemProperty("POSTGRES_JDBC_URL", jdbcUrl)
            .withSystemProperty("POSTGRES_R2DBC_URL", r2dbcUrl)
            .withSystemProperty("POSTGRES_USER", POSTGRES.username)
            .withSystemProperty("POSTGRES_PASS", POSTGRES.password)
    }

    @BeforeEach
    fun cleanup() {
        testSubscriptionRepository.deleteAll()
    }

    @Test
    fun `should create subscription successfully`() {
        runBlocking {
            val request = SubscribeRequest(followerId = 1L, followingId = 2L)
            val response = subscriptionService.subscribe(request)
            assertEquals("OK", response.msg)
            val following = subscriptionService.getFollowing(1L)
            assertEquals(listOf(2L), following)
        }
    }

    @Test
    fun `should fail when subscribing to self`() {
        runBlocking {
            val request = SubscribeRequest(followerId = 1L, followingId = 1L)

            val exception = assertThrows<HttpServerResponseException> {
                subscriptionService.subscribe(request)
            }

            assertEquals(400, exception.code())
            assertTrue(exception.message?.contains("Cannot subscribe to yourself") ?: false)

            val following = subscriptionService.getFollowing(1L)
            assertTrue(following.isEmpty())
        }
    }

    @Test
    fun `should fail when subscription already exists`() {
        runBlocking {
            val request = SubscribeRequest(followerId = 1L, followingId = 2L)

            subscriptionService.subscribe(request)

            val exception = assertThrows<HttpServerResponseException> {
                subscriptionService.subscribe(request)
            }

            assertEquals(409, exception.code())
            assertTrue(exception.message?.contains("Subscription already exists") ?: false)

            val following = subscriptionService.getFollowing(1L)
            assertEquals(1, following.size)
            assertEquals(2L, following[0])
        }
    }

    @Test
    fun `should unsubscribe successfully`() {
        runBlocking {
            val subscribeRequest = SubscribeRequest(followerId = 1L, followingId = 2L)
            subscriptionService.subscribe(subscribeRequest)

            var following = subscriptionService.getFollowing(1L)
            assertEquals(1, following.size)

            subscriptionService.unsubscribe(1L, 2L)

            following = subscriptionService.getFollowing(1L)
            assertTrue(following.isEmpty())

            val followers = subscriptionService.getFollowers(2L)
            assertTrue(followers.isEmpty())
        }
    }

    @Test
    fun `should not affect other subscriptions when unsubscribing`() {
        runBlocking {
            subscriptionService.subscribe(SubscribeRequest(1L, 2L))
            subscriptionService.subscribe(SubscribeRequest(1L, 3L))
            subscriptionService.subscribe(SubscribeRequest(1L, 4L))

            subscriptionService.unsubscribe(1L, 3L)

            val following = subscriptionService.getFollowing(1L)
            assertEquals(2, following.size)
            assertTrue(following.contains(2L))
            assertTrue(following.contains(4L))
            assertFalse(following.contains(3L))

            val followers2 = subscriptionService.getFollowers(2L)
            assertEquals(1, followers2.size)
            assertEquals(1L, followers2[0])

            val followers4 = subscriptionService.getFollowers(4L)
            assertEquals(1, followers4.size)
            assertEquals(1L, followers4[0])
        }
    }

    @Test
    fun `should get following list for user`() {
        runBlocking {
            subscriptionService.subscribe(SubscribeRequest(1L, 2L))
            subscriptionService.subscribe(SubscribeRequest(1L, 3L))
            subscriptionService.subscribe(SubscribeRequest(1L, 4L))

            val following = subscriptionService.getFollowing(1L)

            assertEquals(3, following.size)
            assertTrue(following.contains(2L))
            assertTrue(following.contains(3L))
            assertTrue(following.contains(4L))
        }
    }

    @Test
    fun `should get empty following list for user with no subscriptions`() {
        runBlocking {
            val following = subscriptionService.getFollowing(1L)
            assertTrue(following.isEmpty())
        }
    }

    @Test
    fun `should get followers list for user`() {
        runBlocking {
            subscriptionService.subscribe(SubscribeRequest(1L, 10L))
            subscriptionService.subscribe(SubscribeRequest(2L, 10L))
            subscriptionService.subscribe(SubscribeRequest(3L, 10L))

            val followers = subscriptionService.getFollowers(10L)

            assertEquals(3, followers.size)
            assertTrue(followers.contains(1L))
            assertTrue(followers.contains(2L))
            assertTrue(followers.contains(3L))
        }
    }

    @Test
    fun `should get empty followers list for user with no followers`() {
        runBlocking {
            val followers = subscriptionService.getFollowers(10L)
            assertTrue(followers.isEmpty())
        }
    }

    @Test
    fun `should handle multiple subscriptions from different users`() {
        runBlocking {
            subscriptionService.subscribe(SubscribeRequest(1L, 2L))
            subscriptionService.subscribe(SubscribeRequest(1L, 3L))
            subscriptionService.subscribe(SubscribeRequest(1L, 4L))

            subscriptionService.subscribe(SubscribeRequest(2L, 3L))
            subscriptionService.subscribe(SubscribeRequest(2L, 4L))

            subscriptionService.subscribe(SubscribeRequest(3L, 4L))

            val following1 = subscriptionService.getFollowing(1L)
            assertEquals(3, following1.size)
            assertTrue(following1.containsAll(listOf(2L, 3L, 4L)))

            val following2 = subscriptionService.getFollowing(2L)
            assertEquals(2, following2.size)
            assertTrue(following2.containsAll(listOf(3L, 4L)))

            val followers4 = subscriptionService.getFollowers(4L)
            assertEquals(3, followers4.size)
            assertTrue(followers4.containsAll(listOf(1L, 2L, 3L)))

            val followers3 = subscriptionService.getFollowers(3L)
            assertEquals(2, followers3.size)
            assertTrue(followers3.containsAll(listOf(1L, 2L)))
        }
    }

    @Test
    fun `should allow resubscription after unsubscribe`() {
        runBlocking {
            val request = SubscribeRequest(followerId = 1L, followingId = 2L)

            subscriptionService.subscribe(request)
            assertEquals(1, subscriptionService.getFollowing(1L).size)

            subscriptionService.unsubscribe(1L, 2L)
            assertEquals(0, subscriptionService.getFollowing(1L).size)

            val response = subscriptionService.subscribe(request)
            assertEquals("OK", response.msg)
            assertEquals(1, subscriptionService.getFollowing(1L).size)
        }
    }
}