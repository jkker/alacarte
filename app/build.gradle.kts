plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

android {
    namespace = "dev.alacarte"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.alacarte"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            // 1. Define the Standard Location
            val homeDir = System.getProperty("user.home")
            val keyStoreFile = java.io.File("$homeDir/.config/android-signing/release.jks")

            // 2. Load passwords from Global Gradle Properties (~/.gradle/gradle.properties)
            // Gradle automatically loads these into the project scope!
            val keyStorePass = project.findProperty("ORG_KEYSTORE_PASS") as? String
            val keyAliasName = project.findProperty("ORG_KEY_ALIAS") as? String
            val keyAliasPass = project.findProperty("ORG_KEY_ALIAS_PASS") as? String

            if (keyStoreFile.exists() && keyStorePass != null) {
                storeFile = keyStoreFile
                storePassword = keyStorePass
                keyAlias = keyAliasName
                keyPassword = keyAliasPass
            } else {
                println("⚠️ Release Keystore not found at $keyStoreFile. Skipping signing config.")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.yukihookapi.api)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
    implementation("androidx.annotation:annotation:1.9.1")
    compileOnly(libs.xposed.api)
    ksp(libs.yukihookapi.ksp.xposed)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint("1.0.1")
    }
    kotlinGradle {
        target("*.kts")
        ktlint("1.0.1")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    baseline = file("$projectDir/detekt-baseline.xml")
}
