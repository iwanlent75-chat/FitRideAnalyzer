plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "nl.iwvan.fitride"
 compileSdk = 35
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
 defaultConfig { applicationId = "nl.iwvan.fitride"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1" }
}
dependencies {
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.appcompat:appcompat:1.7.0")
 implementation("androidx.activity:activity-ktx:1.10.0")
 implementation("androidx.documentfile:documentfile:1.0.1")
 implementation("com.google.android.material:material:1.12.0")
 implementation("com.garmin:fit:21.214.0")
}
