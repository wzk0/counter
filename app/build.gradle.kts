import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

// Release signing: values come from keystore.properties (local, never committed) or from
// -P properties (CI). Without them the release build stays unsigned instead of failing.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
  if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { load(it) }
  }
}

fun signingValue(key: String): String? =
  (findProperty(key) as String?)?.takeIf { it.isNotBlank() }
    ?: keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }

val releaseKeystore = signingValue("storeFile")?.let { rootProject.file(it) }
val hasReleaseSigning = releaseKeystore != null && releaseKeystore.exists()

android {
  namespace = "com.example.counter"
  compileSdk = 36
  defaultConfig {
    applicationId = "github.wzk0.counter"
    minSdk = 24
    targetSdk = 36
    // CI passes the tag name / run number so every release carries its own version.
    versionCode = (findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
    versionName = (findProperty("versionName") as String?) ?: "1.0"
  }

  signingConfigs {
    if (hasReleaseSigning) {
      create("release") {
        storeFile = releaseKeystore
        storePassword = signingValue("storePassword")
        keyAlias = signingValue("keyAlias")
        keyPassword = signingValue("keyPassword")
        enableV1Signing = true
        enableV2Signing = true
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    aidl = false
    buildConfig = false
    shaders = false
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

}
