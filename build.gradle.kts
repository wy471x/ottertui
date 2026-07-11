plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("jacoco")
    id("checkstyle")
}

allprojects {
    group = "io.github.ottertui"
    version = "0.1.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/central") }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")

    jacoco {
        toolVersion = "0.8.14"
    }

    java {
        // toolchain/version intentionally unspecified — uses the current JVM.
        // The settings.gradle.kts conditional excludes FFM on JDK <22.
    }

    checkstyle {
        toolVersion = "10.21.1"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        configDirectory = rootProject.file("config/checkstyle")
        // maxErrors = 0            (default: never allow errors)
        // maxWarnings = Int.MAX    (default: unlimited; ratchet down toward 0 over time)
        // isIgnoreFailures = false (default: report warnings but don't fail build)
    }

    tasks.named("check") {
        dependsOn(tasks.named("checkstyleMain"))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher:6.1.1")
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named("test"))
        reports {
            xml.required = true
            html.required = true
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("test"))
        violationRules {
            rule {
                element = "BUNDLE"
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal("0.70")
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn(tasks.named("jacocoTestCoverageVerification"))
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name = "ottertui-${project.name}"
                    description = "OtterTUI - A modern Java terminal UI library"
                    url = "https://github.com/ottertui/ottertui"
                    licenses {
                        license {
                            name = "MIT"
                            url = "https://opensource.org/licenses/MIT"
                        }
                    }
                    scm {
                        url = "https://github.com/ottertui/ottertui"
                        connection = "scm:git:https://github.com/ottertui/ottertui.git"
                    }
                    developers {
                        developer {
                            id = "ottertui"
                            name = "OtterTUI contributors"
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "CentralPortal"
                url = uri("https://central.sonatype.com/api/v1/publisher")
                credentials {
                    username = findProperty("sonatypeUsername") as String? ?: ""
                    password = findProperty("sonatypePassword") as String? ?: ""
                }
            }
        }
    }

    signing {
        val signingKey = findProperty("signingKey") as String? ?: ""
        val signingPassword = findProperty("signingPassword") as String? ?: ""
        if (signingKey.isNotEmpty()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["maven"])
        }
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
