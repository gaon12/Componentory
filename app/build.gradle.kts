plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "xyz.gaon.componentory"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "xyz.gaon.componentory"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "MATERIAL2_VERSION",
            "\"${libs.versions.composeMaterial2.get()}\"",
        )
        buildConfigField(
            "String",
            "MATERIAL3_VERSION",
            "\"${libs.versions.composeMaterial3.get()}\"",
        )
    }

    buildTypes { release { optimization { enable = false } } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    // Pin the sample libraries so the displayed version cannot drift through BOM resolution.
    implementation(libs.androidx.compose.material) {
        version { strictly(libs.versions.composeMaterial2.get()) }
    }
    implementation(libs.androidx.compose.material3) {
        version { strictly(libs.versions.composeMaterial3.get()) }
    }
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
