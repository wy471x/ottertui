allprojects {
    group = "io.github.ottertui"
    version = "0.1.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/central") }
    }
}

/**
 * Installs the shared pre-commit hook from .githooks/ into the local git config.
 * Run once per clone: ./gradlew installGitHook
 */
tasks.register("installGitHook") {
    description = "Configures git to use .githooks/ as the hooks directory"
    group = "setup"
    doLast {
        val hooksDir = rootProject.file(".githooks").absolutePath
        val pb = ProcessBuilder("git", "config", "core.hooksPath", hooksDir)
            .inheritIO()
        pb.start().waitFor()
        println("Git hooks installed from: $hooksDir")
    }
}
