plugins {
    id("dev.kikugie.loom-back-compat")

    // `maven-publish`
    id("me.modmuss50.mod-publish-plugin")

    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

version = "${property("mod.version")}+${property("mod.mc_title")}"
base.archivesName = property("mod.id") as String

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

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

    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    // FIXME: Remove and uncomment terraformersmc maven when (or "if" atp) it's fixed.
    strictMaven("https://maven.gnomecraft.net/releases", "Terraformers", "com.terraformersmc")
    //maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
    maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven("https://maven.siphalor.de/") { name = "Siphalor's Maven" }
}

loom {
    splitEnvironmentSourceSets()

    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runs {
        named("client") {
            client()
            generateRunConfig.set(true)
            jvmArguments.add("-Dmixin.debug.export=true")
            runDirectory.set(file("run"))
        }
        named("server") {
            server()
            generateRunConfig.set(true)
            jvmArguments.add("-Dmixin.debug.export=true")
            runDirectory.set(file("run"))
        }
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
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(
            fabricApi.module(
                it,
                "${property("deps.fabric_api")}+${sc.current.version}"
            )
        )
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")

    if (sc.current.parsed < "26.1") {
        @Suppress("UnstableApiUsage")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${sc.current.version}:${property("parchment")}@zip")
        })
    }

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    implementation("io.github.llamalad7:mixinextras-fabric:${property("deps.mixinextras")}")

    modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-fabric")

    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")

    modImplementation(
        "de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-mc${property("deps.amecs-mc")}:${
            property(
                "deps.amecs-key-modifiers"
            )
        }"
    )

    modCompileOnly("com.moulberry:lattice:${property("deps.lattice")}")
    modCompileOnly("maven.modrinth:axiom:${property("deps.axiom")}")

    fapi(
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1"
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

    register<Copy>("buildAndCollect") {
        group = "build"
        from(loomx.modJar.map { it.archiveFile }, loomx.modSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

publishMods {
    file = loomx.modJar.map { it.archiveFile.get() }
    additionalFiles.from(loomx.modSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.version")} for Minecraft ${property("mod.mc_title")}"
    version = project.version.toString()
    modLoaders.add("fabric")

    modrinth {
        projectId = property("publish.modrinth") as String
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))

        requires { slug = "amecs" }
        requires { slug = "yacl" }
        requires { slug = "fabric-api" }
        optional { slug = "axiom" }
    }

    github {
        parent(project(":").tasks.named("publishGithub"))
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        minecraftVersions.addAll(property("mod.mc_targets").toString().split(' '))

        server = true
        client = true

        requires { slug = "amecs" }
        requires { slug = "yacl" }
        requires { slug = "fabric-api" }
        optional { slug = "axiomtool" }
    }
}
