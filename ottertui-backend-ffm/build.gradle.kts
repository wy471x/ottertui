tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:preview")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
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
