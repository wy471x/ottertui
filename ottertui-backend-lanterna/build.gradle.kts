plugins {
    id("ottertui.java-conventions")
}

dependencies {
    api(project(":ottertui-core"))
    implementation(libs.lanterna)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
