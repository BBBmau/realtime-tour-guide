plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-android")
}

android {
    namespace = "com.mau.exploreai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mau.exploreai"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation("androidx.car.app:app:1.2.0")
    implementation ("androidx.car.app:app-automotive:1.2.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(project(":shared"))
    implementation ("androidx.car:car:1.0.0-alpha1")  // Add this line

    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}