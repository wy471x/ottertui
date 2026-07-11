dependencies {
    api(project(":ottertui-core"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.test {
    val jdkVersion = System.getProperty("java.version")
    if (jdkVersion.split(".").first().toInt() >= 16) {
        jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    }
}
