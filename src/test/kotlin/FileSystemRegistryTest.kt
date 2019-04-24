import io.kotlintest.matchers.collections.containExactly
import io.kotlintest.matchers.collections.containExactlyInAnyOrder
import io.kotlintest.matchers.containAll
import io.kotlintest.matchers.haveSize
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File
import java.time.Instant
import java.util.*

class FileSystemRegistryTest : StringSpec() {

  final class ArtifactGenerator(val groupPrefix: String = "group") : Gen<Artifact> {
    fun artifact(string: String) =
      Artifact(
        "$groupPrefix-$string",
        "name-${string.replace(File.separator, "_")}",
        "contentHash-${UUID.randomUUID()}",
        listOf(
          Source(string, Instant.now(), string),
          Source(string, Instant.now(), string)
        )
      )

    override fun constants() = Gen.string().constants().map { artifact(it) }
    override fun random() = Gen.string().random().map { artifact(it) }
  }

  init {
    "Save and Load" {
      val tempDir = createTempDir()
      println(tempDir)
      val registry = FileSystemRegistry(tempDir)

      assertAll(ArtifactGenerator()) { artifact: Artifact ->
        run {
          val saved = registry.write(artifact)
          val loaded = registry.read(saved)
          loaded shouldBe artifact
        }
      }
    }
  }

  init {
    "List by Prefix" {
      val tempDir = createTempDir()
      println(tempDir)
      val registry = FileSystemRegistry(tempDir)

      val artifactGenerator = ArtifactGenerator()
      val artifacts = artifactGenerator.constants().union(artifactGenerator.random().take(100).toList())
      artifacts.forEach { registry.write(it) }

      val prefixGenerator = ArtifactGenerator("prefix")
      val prefixArtifacts = prefixGenerator.constants().union(prefixGenerator.random().take(100).toList())
      prefixArtifacts.forEach { registry.write(it) }

      val allList = registry.list("")
      allList should haveSize(artifacts.size + prefixArtifacts.size)
      allList should containAll(artifacts)
      allList should containAll(prefixArtifacts)

      val prefixList = registry.list("prefix")
      prefixList should containExactlyInAnyOrder(prefixArtifacts.toList())
    }
  }
}
