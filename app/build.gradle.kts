plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.schemaguard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.schemaguard"
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

    buildFeatures {
        viewBinding = true
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
    val koinVersion = "3.5.3"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Koin DI
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-android-compat:$koinVersion")
    implementation("io.insert-koin:koin-androidx-navigation:$koinVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // GSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Kotlin Reflect
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Gradle task to build downstream dependency tree
tasks.register<JavaExec>("buildDependencyTree") {
    group = "schema"
    description = "Builds downstream dependency tree for all @GenerateJsonSchema classes"
    mainClass.set("com.example.schemaguard.DependencyTreeBuilder")
    dependsOn("compileDebugKotlin", "compileDebugJavaWithJavac")

    doFirst {
        val android = project.extensions.getByName("android") as com.android.build.gradle.AppExtension
        val debugVariant = android.applicationVariants.find { it.name == "debug" }

        if (debugVariant != null) {
            classpath = files(
                project.layout.buildDirectory.dir("tmp/kotlin-classes/debug"),
                debugVariant.javaCompileProvider.get().destinationDirectory,
                debugVariant.runtimeConfiguration,
                android.bootClasspath
            )
        }
    }
}

// Gradle task to run the JSON Schema Generator
tasks.register<JavaExec>("generateSchemas") {
    group = "schema"
    description = "Generates JSON schemas from @GenerateJsonSchema annotated classes"
    mainClass.set("com.example.schemaguard.SchemaGenerator")
    dependsOn("compileDebugKotlin", "compileDebugJavaWithJavac")

    doFirst {
        val android = project.extensions.getByName("android") as com.android.build.gradle.AppExtension
        val debugVariant = android.applicationVariants.find { it.name == "debug" }

        if (debugVariant != null) {
            classpath = files(
                project.layout.buildDirectory.dir("tmp/kotlin-classes/debug"),
                debugVariant.javaCompileProvider.get().destinationDirectory,
                debugVariant.runtimeConfiguration,
                android.bootClasspath
            )
        }
    }
}
