plugins {
    id("ottertui.java-conventions")
}

dependencies {
    testImplementation(project(":ottertui-core"))
    testImplementation(project(":ottertui-widgets"))
    testImplementation(project(":ottertui-tui"))
    testImplementation(project(":ottertui-toolkit"))
    testImplementation(project(":ottertui-examples"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
