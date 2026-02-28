plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.moneymaster"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moneymaster"
        minSdk = 26
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    annotationProcessor(libs.androidx.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Gráficos
    implementation(libs.mpandroidchart)

    // Imágenes
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Excel (con exclusión de log4j)
    implementation(libs.poi) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    implementation(libs.poi.ooxml) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }

    //ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    //DotsIndicator
    implementation("com.tbuonomo:dotsindicator:5.0")


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}