import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.streams.toList

data class Artifact(
  val group: String,
  val name: String,
  val contentHash: String,
  val sources: List<Source>
)

data class Source(
  val url: String,
  val created: Instant,
  val maintainer: String
)

class FileSystemRegistry(val rootDir: File) {

  object instantAdapter {
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    @ToJson
    fun toJson(instant: Instant) = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC).format(formatter)

    @FromJson
    fun fromJson(json: String) = OffsetDateTime.parse(json, formatter).toInstant()
  }

  val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(instantAdapter)
    .build()

  val artifactAdapter = moshi.adapter(Artifact::class.java)

  fun write(artifact: Artifact): File {
    val dir = Paths.get(rootDir.canonicalPath, artifact.group, artifact.name)
      .toFile()
    dir.mkdirs()
    val toWrite = File(dir, artifact.contentHash)
    val json = artifactAdapter.toJson(artifact)
    toWrite.writeText(json, StandardCharsets.UTF_8)
    return toWrite
  }

  fun read(toRead: File): Artifact {
    val json = toRead.readText(StandardCharsets.UTF_8)
    return artifactAdapter.fromJson(json)!!
  }

  fun list(groupPrefix: String): List<Artifact> {
    val files = Files.walk(rootDir.toPath())
      .map { it.toFile() }
      .filter { it.isFile }
      .filter { it.toRelativeString(rootDir).startsWith(groupPrefix) }
      .toList()
    return files.map { read(it) }
  }
}
