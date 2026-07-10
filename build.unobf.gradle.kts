plugins {
    id("net.fabricmc.fabric-loom")

    // `maven-publish`
    id("me.modmuss50.mod-publish-plugin")

    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

version = "${property("mod.version")}+${property("mod.mc_title")}"
base.archivesName = property("mod.id") as String

val requiredJava = JavaVersion.VERSION_25

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")

    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
    maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven("https://maven.siphalor.de/") { name = "Siphalor's Maven" }
}

loom {
    splitEnvironmentSourceSets()

    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "run"
    }
}

fletchingTable {
    j52j.register("main") { extension("json", "**/*.json5") }
    lang.create("main") {
        patterns.add("**/assets/entityselectortools/lang/**")
        flatteningMode = "JOIN"
    }
}

dependencies {
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) implementation(fabricApi.module(it, "${property("deps.fabric_api")}+${sc.current.version}"))
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    implementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    implementation("io.github.llamalad7:mixinextras-fabric:${property("deps.mixinextras")}")

    implementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-fabric")

    implementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")

    implementation("de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-mc${property("deps.amecs-mc")}:${property("deps.amecs-key-modifiers")}")
    compileOnly("com.moulberry:lattice:${property("deps.lattice")}") // Used by Axiom's keybinds
    compileOnly("maven.modrinth:axiom:${property("deps.axiom")}")
//    runtimeOnly("maven.modrinth:axiom:${property("deps.axiom")}")

    fapi(
        "fabric-lifecycle-events-v1",
        "fabric-resource-loader-v0",
        "fabric-content-registries-v0",
        "fabric-networking-api-v1",
        "fabric-command-api-v2",
        "fabric-data-attachment-api-v1",
        "fabric-block-getter-api-v2"
    )
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    withType<ProcessResources> {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", version)
        inputs.property("fabric", project.property("deps.fabric_loader"))
        inputs.property("minecraft", project.property("deps.minecraft"))
        inputs.property("yacl", project.property("deps.yacl"))
        inputs.property("amecs_key_modifiers", project.property("deps.amecs-key-modifiers"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to version,
            "fabric" to project.property("deps.fabric_loader"),
            "minecraft" to project.property("deps.minecraft"),
            "yacl" to project.property("deps.yacl"),
            "amecs_key_modifiers" to project.property("deps.amecs-key-modifiers")
        )

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("**/*.kra", "**/*.aseprite")
    }

    named<ProcessResources>("processClientResources") {
        exclude("fabric.mod.json")
    }

    withType<Jar>().configureEach {
        metaInf {
            from(rootProject.file("LICENSE.md"))
            from(rootProject.file("LICENSE.LESSER.md"))
        }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile }, named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

publishMods {
    file = tasks.jar.map { it.archiveFile.get() }
    additionalFiles.from(
        tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() }
    )
    displayName = "${property("mod.version")} for Minecraft ${property("mod.mc_title")}"
    version = project.version.toString()
    modLoaders.add("fabric")

    modrinth {
        projectId = property("publish.modrinth") as String
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))

        requires {
            slug = "amecs"
        }
        requires {
            slug = "yacl"
        }
        requires {
            slug = "fabric-api"
        }
        optional {
            slug = "axiom"
        }
    }

    github {
        parent(project(":").tasks.named("publishGithub"))
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))

        server = true
        client = true

        requires {
            slug = "amecs"
        }
        requires {
            slug = "yacl"
        }
        requires {
            slug = "fabric-api"
        }
        optional {
            slug = "axiomtool"
        }
    }
}

/*
// Publishes builds to a maven repository under `com.example:template:0.1.0+mc`
publishing {
    repositories {
        maven("https://maven.example.com/releases") {
            name = "myMaven"
            // To authenticate, create `myMavenUsername` and `myMavenPassword` properties in your Gradle home properties.
            // See https://stonecutter.kikugie.dev/wiki/tips/properties#defining-properties
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.id") as String
            version = project.version

            from(components["java"])
        }
    }
}
 */
