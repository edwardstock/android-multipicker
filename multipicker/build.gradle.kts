@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("maven-publish")
    id("signing")
}

val kotlinVersion: String by project
val mpVersion: String by project
val mpMinSdk: String by project
val mpMaxSdk: String by project

group = "com.edwardstock"
version = mpVersion

android {
    namespace = "com.edwardstock.multipicker"
    resourcePrefix = "mp_"

    compileSdk = mpMaxSdk.toInt()

    defaultConfig {
        minSdk = mpMinSdk.toInt()

        testInstrumentationRunner = "android.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
//    kapt("androidx.lifecycle:lifecycle-compiler:2.6.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.constraintlayout:constraintlayout-solver:2.0.4")


//    implementation("com.github.permissions-dispatcher:permissionsdispatcher:4.9.2")
//    kapt("com.github.permissions-dispatcher:permissionsdispatcher-processor:4.9.2")

    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.jsibbold:zoomage:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = project.group as String
            artifactId = "multipicker"
            version = project.version as String

            pom {
                name.set(project.name)
                url.set("https://github.com/edwardstock/android-multipicker")
                description.set("Android gallery image and video picker")
                inceptionYear.set("2018")
                scm {
                    connection.set("scm:git:${pom.url}.git")
                    developerConnection.set(connection)
                    url.set(pom.url)
                }
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/edwardstock/android-multipicker/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("edwardstock")
                        name.set("Eduard Maximovich")
                        email.set("edward.vstock@gmail.com")
                        roles.add("developer")
                    }
                }
            }
        }
    }

    repositories {
        mavenLocal()
        if (hasProperty("ossrhUsername") && hasProperty("ossrhPassword")) {
            maven(url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")) {
                credentials.username = findProperty("ossrhUsername") as String?
                credentials.password = findProperty("ossrhPassword") as String?
                name = "sonatype"
            }
        }
    }
}

project.tasks.withType<PublishToMavenLocal> {
    dependsOn("publishAllPublicationsToMavenLocalRepository")
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}