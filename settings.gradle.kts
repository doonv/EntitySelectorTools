pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.10-alpha.1"
    id("dev.kikugie.loom-back-compat") version "0.4"
}

stonecutter {
    create(rootProject) {
        versions("1.21.10", "1.21.11", "26.1.2", "26.2")
        vcsVersion = "26.2"
    }
}

rootProject.name = "Entity Selector Tools"
