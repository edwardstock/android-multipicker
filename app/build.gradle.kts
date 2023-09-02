@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

val kotlinVersion: String by project
val mpVersion: String by project
val mpMinSdk: String by project
val mpMaxSdk: String by project
android {
    namespace = "com.edwardstock.appmultipicker"
    compileSdk = mpMaxSdk.toInt()
    defaultConfig {
        applicationId = "com.edwardstock.appmultipicker"
        minSdk = mpMinSdk.toInt()
        targetSdk = mpMaxSdk.toInt()
        versionCode = 1
        versionName = mpVersion
        testInstrumentationRunner = "android.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":multipicker"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
//    kapt("androidx.lifecycle:lifecycle-compiler:2.6.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.jakewharton.timber:timber:5.0.1")
    testImplementation("junit:junit:4.13.2")
}
repositories {
    mavenCentral()
}
