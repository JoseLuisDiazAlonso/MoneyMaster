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

    packagingOptions {
        resources.excludes += "META-INF/NOTICE.md"
        resources.excludes += "META-INF/LICENSE.md"
        resources.excludes += "META-INF/NOTICE"
        resources.excludes += "META-INF/LICENSE"
        // Necesarias para iText7
        resources.excludes += "META-INF/versions/9/module-info.class"
        resources.excludes += "META-INF/*.kotlin_module"
        resources.excludes += "mozilla/public-suffix-list.txt"
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
    annotationProcessor(libs.androidx.room.compiler)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Gráficos
    implementation(libs.mpandroidchart)

    // Imágenes
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Excel (Apache POI)
    implementation(libs.poi) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }
    implementation(libs.poi.ooxml) {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }

    // PDF (iText7)
    implementation(libs.itext7.kernel) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.itext7.layout) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.itext7.io) {
        exclude(group = "org.bouncycastle")
    }

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // DotsIndicator
    implementation("com.tbuonomo:dotsindicator:5.0")

    // JavaMail API
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // CoordinatorLayout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // ExifInterface
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // PhotoView
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}