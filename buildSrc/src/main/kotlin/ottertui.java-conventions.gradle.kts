plugins {
    `java-library`
    checkstyle
    `maven-publish`
    signing
}

java {
    // Toolchain intentionally unspecified — uses the current JVM.
    // The FFM backend module guards itself on JDK < 22.
}

checkstyle {
    toolVersion = "10.21.1"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    configDirectory = rootProject.file("config/checkstyle")
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
}

dependencies {
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.1")
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
