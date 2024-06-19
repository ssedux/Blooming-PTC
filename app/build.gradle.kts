plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "proyecto.expotecnica.blooming"
    compileSdk = 34


    defaultConfig {
        applicationId = "proyecto.expotecnica.blooming"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        exclude ("META-INF/NOTICE.md")
        exclude ("META-INF/LICENSE.md")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    testImplementation(libs.junit)
    implementation("com.oracle.database.jdbc:ojdbc6:11.2.0.4")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase Authentication
    implementation ("com.google.firebase:firebase-auth-ktx:21.0.3")
    implementation ("com.google.android.gms:play-services-auth:20.0.1")

    //Dependecias que nos permitira el envio de correos (código de seguridad)
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    //Dependencia de Android Keystore
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")
}