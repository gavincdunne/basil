import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    kotlin.mpp.applyDefaultHierarchyTemplate=false

    androidTarget() {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                // Voyager core navigation
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.core)

                // Optional: Material3 integration
                implementation(libs.voyager.tab.navigator)
                implementation(libs.voyager.bottom.sheet.navigator)

                implementation(libs.androidx.material.icons.extended)
            }
        }
        val androidMain by getting {
            kotlin.srcDirs("src/androidMain/kotlin")

            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.androidDriver)
                implementation(libs.sqldelight.coroutines)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
//                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val iosX64Main by getting {
            kotlin.srcDir("src/iosX64Main/kotlin")
        }

        val iosArm64Main by getting {
            kotlin.srcDir("src/iosArm64Main/kotlin")
        }

        val iosSimulatorArm64Main by getting {
            kotlin.srcDir("src/iosSimulatorArm64Main/kotlin")
        }

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
//                implementation(libs.sqldelight.native.driver)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "org.weekendware.basil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.weekendware.basil"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.weekendware.basil.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.weekendware.basil"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("BasilDatabase") {
            packageName.set("org.weekendware.basil.database")
        }
    }
}



