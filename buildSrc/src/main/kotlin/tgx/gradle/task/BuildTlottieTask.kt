package tgx.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import tgx.gradle.createEmptyDir
import tgx.gradle.fatal
import tgx.gradle.requireDir
import tgx.gradle.requireFile
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class BuildTlottieTask : DefaultTask() {
  @get:Internal
  abstract val inputDir: DirectoryProperty

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val inputSources: ConfigurableFileCollection

  @get:Input
  abstract val abi: Property<String>

  @get:Input
  abstract val rustVersion: Property<String>

  @get:Internal
  abstract val cargoHome: DirectoryProperty

  @get:Internal
  abstract val rustupHome: DirectoryProperty

  @get:Internal
  abstract val buildDir: DirectoryProperty

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Inject
  abstract val exec: ExecOperations

  @get:Inject
  abstract val fs: FileSystemOperations

  @TaskAction
  fun buildTlottie() {
    val input = requireDir(inputDir.get().asFile)
    val cargoHome = requireDir(cargoHome.get().asFile)
    val rustupHome = requireDir(rustupHome.get().asFile)
    val executableSuffix = if (
      System.getProperty("os.name").startsWith("windows", true)
    ) ".exe" else ""
    val cargo = requireFile(cargoHome.resolve("bin/cargo$executableSuffix"))
    val target = when (val abi = abi.get()) {
      "arm64-v8a" -> "aarch64-linux-android"
      "armeabi-v7a" -> "armv7-linux-androideabi"
      "x86" -> "i686-linux-android"
      "x86_64" -> "x86_64-linux-android"
      else -> error("Unsupported ABI: $abi")
    }
    val build = createEmptyDir(fs, buildDir.get().asFile)
    val output = createEmptyDir(fs, outputDir.get().asFile)
    val logFile = build.resolve("build.log")
    val result = logFile.outputStream().use { log ->
      exec.exec {
        standardOutput = log
        errorOutput = log
        isIgnoreExitValue = true
        workingDir = input
        environment(mapOf(
          "CARGO_HOME" to cargoHome.absolutePath,
          "RUSTUP_HOME" to rustupHome.absolutePath,
          "CARGO_TARGET_DIR" to build.absolutePath,
          "CARGO_ENCODED_RUSTFLAGS" to ""
        ))
        commandLine(
          cargo.absolutePath,
          "+${rustVersion.get()}",
          "rustc",
          "--manifest-path", requireFile(input.resolve("Cargo.toml")).absolutePath,
          "--locked",
          "--profile", "release-nostd",
          "--target", target,
          "--lib",
          "--no-default-features",
          "--features", "cpu,no-std,c-api",
          "--crate-type", "staticlib",
          "--",
          "-C", "metadata=tlottie-staticlib"
        )
      }.exitValue
    }
    if (result != 0) {
      fatal("tlottie build failed [${abi.get()}], see: ${logFile.absolutePath}")
    }
    fs.copy {
      from(requireFile(build.resolve("$target/release-nostd/libtlottie.a")))
      into(output.resolve("lib"))
    }
  }
}
