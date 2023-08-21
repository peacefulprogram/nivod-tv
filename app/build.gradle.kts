plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "io.github.peacefulprogram.nivod_tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.peacefulprogram.nivod_tv"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    kotlin {
        jvmToolchain(17)
    }

    java {
//        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
    }
}

dependencies {

    val roomVersion = "2.5.2"
    val coilVersion = "2.4.0"
    val composeTvVersion = "1.0.0-alpha08"
    val ktorVersion = "2.3.3"
    implementation("androidx.core:core-ktx:1.10.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    implementation("androidx.leanback:leanback:1.0.0")


    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    // paging
    implementation("androidx.paging:paging-compose:3.2.0")

    // compose tv
    implementation("androidx.tv:tv-foundation:$composeTvVersion")
    implementation("androidx.tv:tv-material:$composeTvVersion")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    // coil
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")

    // koin
    implementation("io.insert-koin:koin-core:3.4.2")
    implementation("io.insert-koin:koin-android:3.4.2")
    implementation("io.insert-koin:koin-androidx-compose:3.4.5")


    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.google.code.gson:gson:2.10.1")

    val exoPlayerVersion = "2.19.1"

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation("com.google.android.exoplayer:exoplayer-core:$exoPlayerVersion")
    implementation("com.google.android.exoplayer:exoplayer-hls:$exoPlayerVersion")
    implementation("com.google.android.exoplayer:exoplayer-common:$exoPlayerVersion")
    implementation("com.google.android.exoplayer:extension-leanback:$exoPlayerVersion")
    implementation("com.google.android.exoplayer:extension-okhttp:$exoPlayerVersion")

    implementation("cn.hutool:hutool-crypto:5.8.18")

}