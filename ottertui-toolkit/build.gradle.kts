plugins {
    id("ottertui.java-conventions")
    id("ottertui.jacoco-conventions")
}

dependencies {
    api(project(":ottertui-tui"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(project(":ottertui-backend-jline"))
    testRuntimeOnly(project(":ottertui-backend-lanterna"))
}
