plugins {
    id("ottertui.java-conventions")
}

dependencies {
    api(project(":ottertui-core"))
    implementation(libs.aesh.readline)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
