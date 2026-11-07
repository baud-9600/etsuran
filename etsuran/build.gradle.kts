import com.android.build.api.variant.BuildConfigField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

// A ValueSource so the timestamp is (re)computed on every build, even when the
// configuration cache is reused — a plain config-time value gets frozen into the
// cache entry and goes stale. SOURCE_DATE_EPOCH still wins so reproducible
// (F-Droid) builds stay deterministic.
abstract class BuildTimeValueSource :
    ValueSource<String, ValueSourceParameters.None> {
    override fun obtain(): String {
        val epoch = System.getenv("SOURCE_DATE_EPOCH")?.toLongOrNull()
        val date = if (epoch != null) {
            Date(epoch * 1_000) // Convert seconds to milliseconds
        } else {
            Date()
        }

        val dateFormat = SimpleDateFormat("MMddHHmm")
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Taipei")
        return dateFormat.format(date)
    }
}

// The local release key lives outside version control. `keystore.properties`
// is git-ignored (see .gitignore); copy the one from ~/asdfghasdfgh-signing to
// reuse this key for another app. Without it the release build stays
// unsigned so CI/F-Droid can still assemble.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    compileSdk = 36

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    defaultConfig {
        applicationId = "de.asdfghasdfgh.etsuran"
        minSdk = 24
        // targetSdk 36 defaults predictive back to on, which would stop KEYCODE_BACK
        // reaching BrowserActivity.onKeyDown -> KeyHandler; the manifest opts out
        // via enableOnBackInvokedCallback=false.
        targetSdk = 36
        versionCode = 16_07_01
        versionName = "16.7.1"

        // Google Drive backup sync: an "installed app" OAuth client (not a secret;
        // PKCE, no client secret) with the reversed-client-id custom-scheme redirect.
        // Override locally with -Petsuran.driveOAuthClientId=<id>.apps.googleusercontent.com
        val driveOAuthClientId = project.findProperty("etsuran.driveOAuthClientId")?.toString()
            ?: "540228800389-7e35n5f8vvcf9bfq6lb3ra8ud2vclffk.apps.googleusercontent.com"
        val driveOAuthScheme =
            "com.googleusercontent.apps." + driveOAuthClientId.removeSuffix(".apps.googleusercontent.com")
        buildConfigField("String", "DRIVE_OAUTH_CLIENT_ID", "\"$driveOAuthClientId\"")
        buildConfigField("String", "DRIVE_OAUTH_REDIRECT", "\"$driveOAuthScheme:/oauth2redirect\"")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            // Must stay on `proguard-android-optimize.txt`: dropping it to the plain
            // `proguard-android.txt` saved ~35s on R8 but grew the APK by ~1MB, which is
            // not acceptable for a ~7MB release. Don't re-try this without a new plan.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.txt"
            )
        }
        create("releaseDebuggable") {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.txt"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        // kxml2's service file uses a non-standard comma-separated entry that R8 cannot
        // parse; Android registers KXmlParser/KXmlSerializer via its platform XmlPullParserFactory.
        resources.excludes.add("META-INF/services/org.xmlpull.v1.XmlPullParserFactory")
        // epub4j bundles 330KB of W3C/OEB/DAISY DTDs (86KB in the APK) for its
        // EntityResolver, but Android's parser never fetches external DTDs
        // unless validating — which we don't.
        resources.excludes.add("dtd/**")
    }

    androidResources {

        // Only package the locales the app itself is translated into. Without this,
        // AppCompat (and formerly Material Components) add ~80 extra locale configs
        // to resources.arsc for strings the app never shows in those languages.
        // Keep in sync with values-*/ and TranslationLanguageDialog.showAppLocale().
        localeFilters += listOf(
            "af", "ar", "ca", "cs", "da", "de", "el", "en", "es", "fi", "fr",
            "he", "hu", "in", "it", "ja", "ko", "nl", "no", "pl", "pt",
            "ro", "ru", "sat", "sr", "sv", "tr", "uk", "vi", "zh-rCN", "zh-rTW",
        )
    }

    lint {
        baseline = file("lint-baseline.xml")
        disable.add("MissingTranslation")
        checkReleaseBuilds = false
    }

    splits {
        abi {
            isEnable = true
            reset()
            val abis = (project.findProperty("buildAbis") as String?)?.split(",")
                ?: listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            include(*abis.toTypedArray())
            // Only emit the universal APK when explicitly requested (`-PuniversalApk`).
            // Skipping it saves a full packaging pass for local iteration; the release
            // skill should pass -PuniversalApk when cutting a real release.
            isUniversalApk = project.hasProperty("universalApk")
        }
    }
    namespace = "de.asdfghasdfgh.etsuran"
}

androidComponents {
    onVariants { variant ->
        // Wired as a task input (not read at configuration time), so each build
        // re-evaluates the ValueSource and regenerates BuildConfig without
        // invalidating the configuration cache.
        variant.buildConfigFields?.put(
            "lastCommitTime",
            providers.of(BuildTimeValueSource::class.java) {}
                .map { BuildConfigField("String", "\"$it\"", "build timestamp") }
        )
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":ad-filter"))

    // epub4j (maintained fork of epublib). Android ships xmlpull in the platform, so
    // the transitive xmlpull jar is excluded to avoid duplicate XmlPullParser classes.
    implementation(libs.epub4j.core) {
        exclude(group = "xmlpull")
    }

    // for epub saving: html processing
    implementation(libs.jsoup)
    // Nothing in the app uses AppCompat any more (framework Material theme, framework
    // AlertDialog, fragment DialogFragment). These two still declare it transitively;
    // keeping it off the classpath matters because AGP's aapt keep rules would otherwise
    // pin every view referenced from AppCompat's own layouts (~120 KB of dex).
    implementation(libs.androidx.constraintlayout) {
        exclude(group = "androidx.appcompat")
    }
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.timber)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.compose.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)

    // for dark mode
    implementation(libs.androidx.webkit)

    // SAF folder access (fonts, Supernote storage, EPUB export). Previously came in
    // transitively via Material Components, which is no longer a dependency.
    implementation(libs.androidx.documentfile)

    // Koin
    implementation(libs.koin.core)
    testImplementation(libs.koin.test)
    implementation(libs.koin.android) {
        exclude(group = "androidx.appcompat")
    }

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)
    // Real org.json implementation for JVM unit tests (the android.jar on the
    // unit-test classpath only contains non-functional stubs of JSONObject/JSONArray).
    testImplementation(libs.org.json)
    // Desktop pdfbox validates the in-repo COS-level PDF writer (unit/pdf/),
    // which replaced pdfbox-android in the app itself.
    testImplementation(libs.pdfbox.test)

    // memory leak detection
    //debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")

    // compose
    // Compose Material Design
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.extended)

    // Tooling support (Previews, etc.)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    // UI Tests
    androidTestImplementation(libs.compose.ui.test.junit4)

    implementation(libs.accompanist.drawablepainter)

    // reorder lazylist
    implementation(libs.reorderable)

    // okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.kotlinx.serialization.json)
}
