pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "ottertui"

include("ottertui-core")
include("ottertui-widgets")
include("ottertui-tui")
include("ottertui-backend-jline")
include("ottertui-backend-lanterna")
include("ottertui-backend-aesh")
include("ottertui-backend-ffm")
include("ottertui-toolkit")
include("ottertui-examples")
include("ottertui-integration-tests")
