plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.minervaai.summasphere"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.minervaai.summasphere"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"https://backend-dot-summasphere-capstone.et.r.appspot.com/api/v1/\"")
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation (libs.androidx.recyclerview)

    // Splashscreen
    implementation(libs.androidx.core.splashscreen)

    // Onboarding animation
    implementation (libs.lottie)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase Auth
    implementation(libs.google.firebase.auth)

    // Google Play services
    implementation(libs.gms.play.services.auth)
    implementation (libs.gms.play.services.auth)

    // Navigation Graph
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Encrypted Shared Preference
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")

    // Handling Image
    implementation ("com.squareup.picasso:picasso:2.71828")

    // Handling Markdown
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // PDF
    implementation ("com.itextpdf:itext7-core:7.1.15")
    implementation("com.itextpdf:layout:7.2.0")

    implementation ("androidx.fragment:fragment-ktx:1.8.0")

}