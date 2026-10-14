import org.gradle.api.tasks.testing.Test
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    kotlin("plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.12.1"
}

group = "com.cognitivecreations.japaneselanguagegame"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("japanese-language-game")
        browser {
            commonWebpackConfig {
                outputFileName = "japanese-language-game.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        wasmJsMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.cognitivecreations.japaneselanguagegame.generated.resources"
}

compose.desktop {
    application {
        mainClass = "com.cognitivecreations.japaneselanguagegame.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "JapaneseLanguageGame"
            packageVersion = "1.0.0"
            description = "Japanese sentence-structure study game"
            vendor = "Cognitive Creations"
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register("test") {
    group = "verification"
    description = "Runs the JVM tests (game rules, catalog, and progress storage)."
    dependsOn("jvmTest")
}
