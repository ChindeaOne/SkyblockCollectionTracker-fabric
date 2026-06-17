import sct.GitVersion

plugins {
    id("com.gradleup.shadow") version "9.4.2"
    id("net.fabricmc.fabric-loom")
    kotlin("jvm") version "2.4.0"
    id("com.google.devtools.ksp") version "2.3.9"
    kotlin("plugin.power-assert") version "2.4.0"
}

val gitVersion = objects.newInstance(GitVersion::class)
version = gitVersion.setVersionfromGit(project)

group = project.property("maven_group").toString()

base {
    archivesName.set("${project.property("archives_base_name")}-mc${sc.current.version}")
}

repositories {
    mavenCentral()

    maven("https://jitpack.io")

    // Fabric
    exclusiveContent {
        forRepository {
            maven("https://maven.fabricmc.net")
        }
        filter {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
        }
    }

    // Mixin
    exclusiveContent {
        forRepository {
            maven("https://repo.spongepowered.org/repository/maven-public")
        }
        filter {
            includeGroup("org.spongepowered")
        }
    }

    // DevAuth
    exclusiveContent {
        forRepository {
            maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
        }
        filter {
            includeGroup("me.djtheredstoner")
        }
    }

    // ModMenu
    exclusiveContent {
        forRepository {
            maven(url = "https://maven.terraformersmc.com/releases")
        }

        filter {
            includeGroup("com.terraformersmc")
            includeGroup("dev.emi")
        }
    }

    // Moulconfig
    exclusiveContent {
        forRepository {
            maven("https://maven.notenoughupdates.org/releases")
        }
        filter {
            includeGroup("org.notenoughupdates")
            includeGroup("org.notenoughupdates.moulconfig")
        }
    }

    maven {
        url = uri("https://maven.notenoughupdates.org/releases")
        content {
            includeGroup("org.notenoughupdates.moulconfig")
        }
    }
}

loom {
    accessWidenerPath.set(
        rootProject.file("src/main/resources/skyblockcollectiontracker.accesswidener")
    )
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

fabricApi {
    configureDataGeneration {
        client.set(true)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Fabric API
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
    runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

    implementation("com.terraformersmc:modmenu:${project.property("mod_menu_version")}")

    shadowImpl("org.notenoughupdates.moulconfig:modern-${project.property("moulconfig_version")}") {
        exclude(group = "org.jetbrains.kotlin")
    }
    shadowImpl("com.github.ChindeaOne:modrinthautoupdater:${project.property("modrinthautoupdater_version")}") {
        exclude(group = "gson")
    }
    include("org.notenoughupdates.moulconfig:modern-${project.property("moulconfig_version")}")
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.4"
        }
    }
}

tasks.processResources {
    val expandProps = buildMap {
        put("version", project.version)
        put("minecraft_version", sc.current.version)
        put("TOKEN_URL", System.getenv("TOKEN_URL") ?: "")
        put("TRACKED_COLLECTION_URL", System.getenv("TRACKED_COLLECTION_URL") ?: "")
        put("AVAILABLE_COLLECTIONS_URL", System.getenv("AVAILABLE_COLLECTIONS_URL") ?: "")
        put("AVAILABLE_GEMSTONES_URL", System.getenv("AVAILABLE_GEMSTONES_URL") ?: "")
        put("NPC_PRICES_URL", System.getenv("NPC_PRICES_URL") ?: "")
        put("BAZAAR_URL", System.getenv("BAZAAR_URL") ?: "")
        put("STATUS_URL", System.getenv("STATUS_URL") ?: "")
        put("GITHUB_URL", System.getenv("GITHUB_URL") ?: "")
        put("COLORS_URL", System.getenv("COLORS_URL") ?: "")
        put("SKILLS_URL", System.getenv("SKILLS_URL") ?: "")
        put("COLEWEIGHT_URL", System.getenv("COLEWEIGHT_URL") ?: "")
        put("FARMINGWEIGHT_URL", System.getenv("FARMINGWEIGHT_URL") ?: "")
        put("COLLECTION_LEADERBOARD_URL", System.getenv("COLLECTION_LEADERBOARD_URL") ?: "")
        put("WAYPOINTS_URL", System.getenv("WAYPOINTS_URL") ?: "")
        put("SKILLTREE_URL", System.getenv("SKILLTREE_URL") ?: "")
        put("AGENT", System.getenv("AGENT") ?: "")
    }

    expandProps.forEach { (key, value) ->
        inputs.property(key, value)
    }

    filesMatching(listOf("fabric.mod.json", "assets/skyblockcollectiontracker/url.properties")) {
        expand(expandProps)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("dev")
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    archiveClassifier.set("")

    val archivesNameValue = base.archivesName.get()
    inputs.property("archivesName", archivesNameValue)

    from("LICENSE") {
        rename { "${it}_${archivesNameValue}" }
    }

    configurations = listOf(shadowImpl)

    doLast {
        listOf(shadowImpl,).forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }
    exclude("META-INF/versions/**")
    mergeServiceFiles()
    relocate("io.github.notenoughupdates.moulconfig", "io.github.chindeaone.collectiontracker.deps.moulconfig")
    relocate("io.github.chindeaone.modrinthautoupdater", "io.github.chindeaone.collectiontracker.deps.modrinthautoupdater")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
