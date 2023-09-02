// Top-level build file where you can add configuration options common to all sub-projects/modules.

val kotlinVersion: String by project
val mpVersion: String by project
val mpMinSdk: String by project
val mpMaxSdk: String by project

buildscript {

    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://clojars.org/repo/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")
        maven(url = "https://oss.jfrog.org/libs-snapshot/")
    }
}


//task clean(type: Delete) {
//    delete rootProject.buildDir
//}
