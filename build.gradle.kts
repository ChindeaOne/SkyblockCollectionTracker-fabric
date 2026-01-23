import net.fabricmc.loom.task.RemapJarTask
import sct.GitVersion

plugins {
    id("com.gradleup.shadow") version "9.3.1"
    id("net.fabricmc.fabric-loom-remap")
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.24"
    kotlin("plugin.power-assert") version "2.0.0"
    `maven-publish`
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
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp.set(true)
        defaultRefmapName.set("mixins.sct.refmap.json")
    }
}

val shadowModImpl: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
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
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    modImplementation("com.terraformersmc:modmenu:${project.property("mod_menu_version")}")

    shadowModImpl("org.notenoughupdates.moulconfig:modern-${project.property("moulconfig_version")}")
    shadowImpl("com.github.ChindeaOne:modrinthautoupdater:${project.property("modrinthautoupdater_version")}") {
        exclude(group = "gson")
    }
    include("org.notenoughupdates.moulconfig:modern-${project.property("moulconfig_version")}")
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
            enableLanguageFeature("BreakContinueInInlineLambdas")
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", sc.current.version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to sc.current.version
        )
    }
    filesMatching("assets/skyblockcollectiontracker/url.properties") {
        expand(
            "TOKEN_URL" to (System.getenv("TOKEN_URL") ?: ""),
            "TRACKED_COLLECTION_URL" to (System.getenv("TRACKED_COLLECTION_URL") ?: ""),
            "AVAILABLE_COLLECTIONS_URL" to (System.getenv("AVAILABLE_COLLECTIONS_URL") ?: ""),
            "AVAILABLE_GEMSTONES_URL" to (System.getenv("AVAILABLE_GEMSTONES_URL") ?: ""),
            "NPC_PRICES_URL" to (System.getenv("NPC_PRICES_URL") ?: ""),
            "BAZAAR_URL" to (System.getenv("BAZAAR_URL") ?: ""),
            "STATUS_URL" to (System.getenv("STATUS_URL") ?: ""),
            "AGENT" to (System.getenv("AGENT") ?: "")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    val archivesNameValue = base.archivesName.get()
    inputs.property("archivesName", archivesNameValue)

    from("LICENSE") {
        rename { "${it}_${archivesNameValue}" }
    }
}

val remapJar by tasks.named<RemapJarTask>("remapJar") {
    archiveClassifier.set("")

    archiveFileName.set("${project.property("archives_base_name")}-${project.version}+mc${sc.current.version}.jar")

    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/${sc.current.version}"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl, shadowModImpl)

    doLast {
        listOf(shadowImpl, shadowModImpl).forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }
    exclude("META-INF/versions/**")
    mergeServiceFiles()
    relocate("io.github.notenoughupdates.moulconfig", "io.github.chindeaone.collectiontracker.deps.moulconfig")
    relocate("io.github.chindeaone.modrinthautoupdater", "io.github.chindeaone.collectiontracker.deps.modrinthautoupdater")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name").toString()
            from(components["java"])
        }
    }

    repositories {}
}
