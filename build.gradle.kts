import org.gradle.api.tasks.testing.Test
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.compose.multiplatform)
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test.junit5)
            implementation(libs.kotlinx.coroutines.core)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
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
