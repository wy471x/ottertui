plugins {
    application
}

application {
    mainClass = "com.ottertui.examples.DemoApp"
}

dependencies {
    implementation(project(":ottertui-core"))
    implementation(project(":ottertui-widgets"))
    implementation(project(":ottertui-tui"))
    implementation(project(":ottertui-toolkit"))
    implementation(project(":ottertui-backend-jline"))
    implementation(project(":ottertui-backend-lanterna"))
    runtimeOnly(libs.logback.classic)
}
