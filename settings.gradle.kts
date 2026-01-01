pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://maven.fabricmc.net")
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("(com|io)\\.github\\..*")
            }
        }
    }
}

rootProject.name = "SkyblockCollectionTracker"