plugins {
    id("ottertui.java-conventions")
    id("ottertui.jacoco-conventions")
}

dependencies {
    api(project(":ottertui-core"))
    api(project(":ottertui-widgets"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(project(":ottertui-backend-jline"))
    testRuntimeOnly(project(":ottertui-backend-lanterna"))
    testRuntimeOnly(project(":ottertui-backend-aesh"))
    if (JavaVersion.current() >= JavaVersion.VERSION_22) {
        testRuntimeOnly(project(":ottertui-backend-ffm"))
    }
}
