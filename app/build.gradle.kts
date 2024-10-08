
plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
//    id("kotlin-kapt")
//    id("com.google.dagger.hilt.android")

}

android {
    namespace = "com.example.mychallenge"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mychallenge"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17


    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)


    //Google Fonts
    implementation(libs.androidx.ui.text.google.fonts)

    // LiveData
    implementation(libs.androidx.runtime.livedata)

    // Compose Coil - Image URL
    implementation(libs.coil.compose)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Compose ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    //Serialization
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.firebase.firestore.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose.v273)



    // Retrofit

//    implementation(libs.retrofit)
    implementation (libs.converter.gson)


//    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.androidx.material.icons.extended)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))

    // Firebase Firestore y Auth
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Servicios de autenticación de Google Play
    implementation("com.google.android.gms:play-services-auth:20.6.0")


    // Hilt - Inyección de dependencias


//    implementation (libs.androidx.hilt.navigation.compose.v100)
//    implementation (libs.xhilt.android)
//    kapt (libs.xhilt.android.compiler)
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    // https://firebase.google.com/docs/android/setup#available-libraries
}

// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}

