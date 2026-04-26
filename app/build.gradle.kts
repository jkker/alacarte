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

    val releaseTaskRequested =
        gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

    val keystoreFile = providers.environmentVariable("ANDROID_KEYSTORE_FILE").orNull
    val keystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
    val keyAliasValue = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
    val keyPasswordValue = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
    val keystoreTypeValue = providers.environmentVariable("ANDROID_KEYSTORE_TYPE").orNull

    val hasReleaseSigning =
        !keystoreFile.isNullOrBlank() &&
        !keystorePassword.isNullOrBlank() &&
        !keyAliasValue.isNullOrBlank() &&
        !keyPasswordValue.isNullOrBlank()

    if (hasReleaseSigning) {
        signingConfigs {
            create("releaseFromEnv") {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue

                if (!keystoreTypeValue.isNullOrBlank()) {
                    storeType = keystoreTypeValue
                }
            }
        }

        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("releaseFromEnv")
            }
        }
    } else if (releaseTaskRequested) {
        throw GradleException(
            "Release signing env vars missing. Run `mise build`, not `./gradlew assembleRelease` directly."
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
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
