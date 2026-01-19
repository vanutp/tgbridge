import org.jetbrains.kotlin.gradle.dsl.JvmTarget

operator fun String.invoke(): String =
    rootProject.properties[this] as? String ?: error("Property $this not found")

repositories {
    maven {
        name = "lucko"
        url = uri("https://repo.lucko.me/")
        content {
            includeGroup("me.lucko")
        }
    }
//    maven {
//        name = "KvotheShaed Releases"
//        url = uri("https://maven.kvotheshaed.ru/releases")
//        content {
//            includeGroup("ru.dimaskama.voicemessages")
//        }
//    }
    maven {
        url = uri("https://maven.maxhenkel.de/repository/public")
        content {
            includeGroup("de.maxhenkel.voicechat")
        }
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:${"retrofitVersion"()}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-serialization-core")
        exclude(module = "kotlinx-serialization-json")
    }
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:${"retrofitVersion"()}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    }
    implementation("com.charleskorn.kaml:kaml:${"kamlVersion"()}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    }
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
//    compileOnly("ru.dimaskama.voicemessages:voicemessages-api:0.0.1")
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.5.31")

    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 8
    }

    test {
        useJUnitPlatform()
    }
}
