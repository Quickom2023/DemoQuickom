plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    // 1. Áp dụng plugin Compose Compiler
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.beowulfchain.conferencetv.demoapp"
//    compileSdk = 34
    compileSdk = 36

    defaultConfig {
        applicationId = "com.beowulfchain.conferencetv.demoapp"
//        minSdk = 21
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.1"
//    }
    packaging {
        resources {
            // Giải pháp quan trọng nhất:
            // Ép App Demo chỉ lấy bản đầu tiên của WebRTC mà nó tìm thấy trong SDK
//            pickFirsts.add("org/webrtc/Camera1Helper.class")
//            pickFirsts.add("org/webrtc/**/*.class")

            // Loại bỏ các file META-INF có thể gây xung đột khác
//            excludes.add("META-INF/*.kotlin_module")
//            excludes.add("**/A1/a.class")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dùng repo từ flutter_sdk_packer
    debugImplementation("com.beowulfchain.flutter_sdk_packer:flutter_debug:1.0")
    releaseImplementation("com.beowulfchain.flutter_sdk_packer:flutter_release:1.0")

    // Dùng quickom_sdk.aar
//    implementation(files("../../SDK/quickom_sdk.aar"))

    implementation("androidx.window:window:1.2.0")
    implementation("androidx.appcompat:appcompat:1.6.1")


    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}