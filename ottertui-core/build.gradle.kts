plugins {
    id("ottertui.java-conventions")
    id("ottertui.jacoco-conventions")
}

dependencies {
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
