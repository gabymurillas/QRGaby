plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.qr_prueba_gaby"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.qr_prueba_gaby"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Define secretos desde gradle.properties
        buildConfigField("String", "SHARED_SECRET", "\"${project.findProperty("SHARED_SECRET") ?: ""}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ZXing para generar códigos QR
    implementation(libs.zxing.core)

    // CameraX + ML Kit para escanear el QR de provisionamiento del Administrador
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode)

    // DataStore para persistencia de estado de registro
    implementation(libs.androidx.datastore.preferences)

    // Encriptación AES con Android Keystore
    implementation(libs.androidx.security.crypto)

    // Permisos BLE/Location en Compose
    implementation(libs.accompanist.permissions)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // Íconos extendidos de Material (DirectionsCar, LockOpen, Bluetooth, etc.)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit para validación con el endpoint de Odoo
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Autenticación Biométrica (Huella/FaceID)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
}


