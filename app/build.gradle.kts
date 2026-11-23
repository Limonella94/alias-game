plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Номер версии = число коммитов в git: растёт с каждым изменением, одинаков локально и в CI.
val gitCommitCount: Int = runCatching {
    providers.exec { commandLine("git", "rev-list", "--count", "HEAD") }
        .standardOutput.asText.get().trim().toInt()
}.getOrDefault(1)
val appVersionCode = gitCommitCount
val appVersionName = "0.3.$gitCommitCount"

// Ключ подписи для CI: если задан (из секретов GitHub) — подписываем им, иначе общим debug.keystore.
val ciKeystorePath: String? = System.getenv("SIGNING_KEYSTORE_PATH")
val ciKeystorePassword: String? = System.getenv("SIGNING_PASSWORD")

android {
    namespace = "ru.limonella.alias"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.limonella.alias"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    // Общий debug-ключ в репозитории: APK из GitHub и из Android Studio подписаны одинаково,
    // поэтому одно ставится поверх другого без удаления. Для публикации в магазин будет отдельный ключ.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (ciKeystorePath != null) {
            create("ci") {
                storeFile = file(ciKeystorePath)
                storePassword = ciKeystorePassword
                keyAlias = "alias"
                keyPassword = ciKeystorePassword
            }
        }
    }

    buildTypes {
        debug {
            if (ciKeystorePath != null) signingConfig = signingConfigs.getByName("ci")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
        compose = true
        buildConfig = true
    }
}

// Для CI: печатает "versionCode|versionName", чтобы записать их в version.json.
tasks.register("printVersion") {
    doLast { println("$appVersionCode|$appVersionName") }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
