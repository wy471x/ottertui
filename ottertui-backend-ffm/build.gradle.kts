import org.gradle.api.attributes.Attribute

if (JavaVersion.current() < JavaVersion.VERSION_22) {
    tasks.configureEach { enabled = false }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-restricted")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JacocoReport>("jacocoTestReport") {
    classDirectories.setFrom(files())
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(files())
}

dependencies {
    api(project(":ottertui-core"))
    implementation(libs.jansi)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

// This module compiles with JDK 22+ (java.lang.foreign), but at runtime
// consumers can bundle it alongside other backends — classloaders on
// older JVMs will ignore FFM classes that reference unavailable APIs.
configurations.runtimeElements {
    attributes {
        attribute(Attribute.of("org.gradle.jvm.version", Int::class.javaObjectType), 8)
    }
}
