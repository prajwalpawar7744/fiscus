import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Release signing: keystore path and passwords live in the git-ignored keystore.properties
// at the project root. Without it, release builds come out unsigned.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.prajwalpawar.fiscus"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.prajwalpawar.fiscus"
        minSdk = 26
        targetSdk = 36
        // Bump versionCode for every build you hand out; versionName follows SemVer.
        versionCode = 4
        versionName = "0.1.0-beta.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Only once keystore.properties points at a real keystore file.
        val keystore = keystoreProperties.getProperty("storeFile")
            ?.takeIf { it.isNotBlank() }
            ?.let { rootProject.file(it) }
            ?.takeIf { it.exists() }
        if (keystore != null) {
            create("release") {
                storeFile = keystore
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        // Installs beside the release app (different package and signature), so a signed
        // release and a debug build can both be on the phone.
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Fiscus Debug")
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            // R8 strips unused code (most of Compose and the extended icon set) and resources.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // For the debug build's own app name.
        resValues = true
    }
    // Migration tests read the exported schema JSON files.
    sourceSets {
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
}

// Room writes each database version's schema here, so migrations can be tested.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
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
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}

// Ships CHANGELOG.md (project root) inside the app as an asset, so Settings → Version can
// show "What's new" offline. Copied fresh on every build; the root file stays the only copy.
abstract class CopyChangelogTask : DefaultTask() {
    @get:InputFile
    abstract val changelog: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copy() {
        val target = outputDir.get().asFile.apply { mkdirs() }
        changelog.get().asFile.copyTo(target.resolve("CHANGELOG.md"), overwrite = true)
    }
}

androidComponents {
    onVariants { variant ->
        val copyChangelog = tasks.register<CopyChangelogTask>(
            "copy${variant.name.replaceFirstChar { it.uppercase() }}Changelog"
        ) {
            changelog.set(rootProject.layout.projectDirectory.file("CHANGELOG.md"))
        }
        variant.sources.assets?.addGeneratedSourceDirectory(copyChangelog, CopyChangelogTask::outputDir)
    }
}
