import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget() {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    listOf(
//        iosX64(),
//        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.androidDriver)
        }
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.navigation.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.datetime)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqlite.driver)
        }
        desktopTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.sqlite.driver)
            implementation(libs.mockito.kotlin)
            implementation(libs.kotlinx.coroutinesTest)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
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
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Basil Dev")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            resValue("string", "app_name", "Basil Staging")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "Basil")
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

buildkonfig {
    packageName = "org.weekendware.basil"

    // Prod defaults
    defaultConfigs {
        buildConfigField(STRING, "FLAVOR", "prod")
        buildConfigField(STRING, "BASE_URL", "https://api.basil.app")
    }
    targetConfigs {
        create("dev") {
            buildConfigField(STRING, "FLAVOR", "dev")
            buildConfigField(STRING, "BASE_URL", "https://dev-api.basil.app")
        }
        create("staging") {
            buildConfigField(STRING, "FLAVOR", "staging")
            buildConfigField(STRING, "BASE_URL", "https://staging-api.basil.app")
        }
    }
}

// Per-flavor desktop run tasks
listOf("dev", "staging", "prod").forEach { flavor ->
    tasks.register("runDesktop${flavor.replaceFirstChar { it.uppercase() }}") {
        group = "application"
        description = "Run desktop app with $flavor config"
        dependsOn("desktopRun")
        doFirst {
            System.setProperty("app.flavor", flavor)
        }
    }
}

sqldelight {
    databases {
        create("BasilDatabase") {
            packageName.set("org.weekendware.basil.database")
            verifyMigrations.set(false)
        }
    }
}
