package gay.extremist

import gay.extremist.dao.accountDao
import gay.extremist.dao.tagDao
import gay.extremist.models.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.random.Random

const val TOTAL_ACCOUNTS = 10
val RANDOM = Random.Default

suspend fun main() {
    init()
    val client = HttpClient(CIO)
    for (i in 1..TOTAL_ACCOUNTS) {
        val account = accountDao.readAccount(i)!! // Send multipart form data
        client.submitFormWithBinaryData(url = "http://localhost:8080/videos", formData = formData {
            append("title", RANDOM.nextTitle())
            append("description", RANDOM.nextDescription())
            append("tags", runBlocking {
                tagDao.getPresetTags().shuffled().subList(0, RANDOM.nextInt(7) + 3)
            }.joinToString(", ") {
                return@joinToString it.toResponse().tag
            }) // Add video file
            val random = (1..3).random()
            append("file", File("/home/foxkj/Downloads/video$random.mp4").readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "video/mp4")
                append(HttpHeaders.ContentDisposition, "attachment; filename=\"video$random.mp4\"")
            })
        }) {
            method = HttpMethod.Post
            headers {
                append("accountId", account.id.toString())
                append("token", account.token)
            }
        }

    }
}

private fun Random.Default.nextDescription(): String {
    return "Nobis quisquam voluptas veritatis. Veritatis occaecati quidem itaque fuga ut sint vitae perferendis. Ea aspernatur nam ratione ipsam. Quidem voluptatem officia facilis qui. Dolore ducimus consequatur delectus.\n" + "\n" + "Impedit sunt amet voluptas mollitia et ab necessitatibus. Sit asperiores sunt neque commodi in. Repudiandae voluptates magnam sed facere quam. Molestiae rem et et et ut laborum eveniet.\n" + "\n" + "Provident unde velit hic dolorum iste voluptas. Aliquam qui corrupti nam repellendus totam aut magnam. Maxime aut ipsum debitis impedit quibusdam incidunt magnam. Quam tempore provident id. Cum et quia excepturi dolorem quo et. Tenetur dolorum fugit ea explicabo sit impedit reprehenderit.\n" + "\n" + "Autem minus aut numquam neque. Qui iusto nesciunt totam tempora voluptas. Fugit maxime quae dolorum ad laborum perspiciatis vel. Quasi quae qui repudiandae magnam quae dolorem ducimus eaque. Quia sed nesciunt quod veniam omnis reiciendis rerum dolore."
}

val titlesList = listOf(
    "Adorable Kittens Exploring",
    "Crafting Adventures: Minecraft Edition",
    "Serene Strolls: Nature's Beauty",
    "Delicious Recipes for Busy Days",
    "Tech Talk: Latest Gadgets Unboxed",
    "Fitness Journey: One Step at a Time",
    "Artistic Creations: Painting Bliss",
    "Thrilling Tales: Mystery Unraveled",
    "Home Decor Tips and Tricks",
    "DIY Projects Made Easy",
    "Travel Diaries: Wanderlust Chronicles",
    "Music Melodies: Soulful Beats",
    "Health and Wellness: Mindful Living",
    "Fashion Forward: Style Trends Revealed",
    "Bookworm's Paradise: Literary Escapes",
    "Pet Care Essentials: Happy Pets, Happy Life",
    "Tech Tips: Simplifying Tech Troubles",
    "Gaming Galore: Gaming News and Reviews",
    "Parenting Pointers: Navigating Parenthood",
    "Financial Fitness: Money Matters Made Simple",
    "Eco-Friendly Living: Green Choices for a Better World",
    "Career Insights: Professional Growth Strategies",
    "Foodie Adventures: Culinary Delights Explored",
    "Mindfulness Moments: Finding Inner Peace",
    "Photography Passion: Capturing Moments in Time",
    "Fitness Freak: Workout Routines and Tips",
    "Educational Escapades: Learning Made Fun",
    "Vintage Vibes: Nostalgic Nods to the Past",
    "Celebrity Scoop: Hollywood Gossip Unveiled",
    "Home Improvement Hacks: Upgrade Your Space"
)

private fun Random.Default.nextTitle(): String {
    return titlesList.random()
}

fun init() {
    val database = Database.connect(
        url = """
            jdbc:postgresql://${
            System.getenv("DB_HOST") ?: "localhost"
        }:${
            System.getenv("DB_PORT") ?: "5432"
        }/${
            System.getenv("DB_NAME") ?: "capstone_db"
        }
            """.trimIndent(),
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "12345678"
    )
    transaction(database) {
        SchemaUtils.create(
            Accounts,
            AccountFollowsAccount,
            AccountFollowsTag,
            Playlists,
            PlaylistContainsVideo,
            Videos,
            Comments,
            Ratings,
            Tags,
            TagLabelsVideo
        )

        exec("CREATE EXTENSION IF NOT EXISTS pg_trgm;")
        exec("CREATE INDEX IF NOT EXISTS video_title_gin_idx ON videos USING GIN(title gin_trgm_ops);")
        exec("CREATE INDEX IF NOT EXISTS account_username_gin_idx ON accounts USING GIN(username gin_trgm_ops);")
    }
}