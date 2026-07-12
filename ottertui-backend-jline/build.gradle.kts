plugins {
    id("ottertui.java-conventions")
}

dependencies {
    api(project(":ottertui-core"))
    implementation(libs.jline.terminal)
    implementation(libs.jline.reader)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
