import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// La configuración que cambia por máquina NO va en el código: se lee de local.properties (no se
// sube a Git) o de gradle.properties / -P. Ver docs/guia-backend.md §App.
//
//   api.url          dónde está la API. Emulador: http://10.0.2.2:8000 · Teléfono: la URL del túnel.
//   api.url.release  la de la versión de entrega (solo HTTPS): la del túnel o la del servidor.
//                    Si no se pone, se usa api.url.
//   enlaces.dominio  el dominio de los enlaces para compartir, cuando haya uno propio (App Links)
//   firma.archivo, firma.alias, firma.contrasena
//                    la llave con la que se firma el APK de entrega. Vive FUERA del repo; quien no
//                    la tenga compila igual, pero su APK release sale sin firmar.
//
// Ningún secreto va aquí: lo que se compila dentro del APK lo puede leer cualquiera. El secreto
// que firma los JWT vive solo en el servidor (backend/.env).
val locales = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun config(nombre: String, porOmision: String = ""): String =
    (project.findProperty(nombre) as String?) ?: locales.getProperty(nombre) ?: porOmision

android {
    namespace = "mx.tec.familiasquesuman"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "mx.tec.familiasquesuman"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_URL", "\"${config("api.url", "http://10.0.2.2:8000")}\"")
        buildConfigField("String", "ENLACES_DOMINIO", "\"${config("enlaces.dominio", "enlaces.familiasquesuman.invalid")}\"")

        // Lo usa el manifiesto (los enlaces que abren la app). ".invalid" nunca existe: sin
        // dominio propio, el filtro de App Links queda inofensivo.
        manifestPlaceholders["enlacesDominio"] = config("enlaces.dominio", "enlaces.familiasquesuman.invalid")
    }

    // La firma del APK de entrega. Android solo instala apps firmadas, y cada actualización debe ir
    // firmada con la MISMA llave: perderla = no poder actualizar la app. Respáldala.
    val llave = config("firma.archivo").takeIf { it.isNotBlank() }?.let { rootProject.file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (llave != null) {
            create("entrega") {
                storeFile = llave
                storePassword = config("firma.contrasena")
                keyAlias = config("firma.alias")
                keyPassword = config("firma.contrasena")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (llave != null) signingConfig = signingConfigs.getByName("entrega")
            buildConfigField("String", "API_URL", "\"${config("api.url.release", config("api.url", "http://10.0.2.2:8000"))}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // La API: Retrofit + OkHttp, con kotlinx.serialization para el JSON.
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
}
