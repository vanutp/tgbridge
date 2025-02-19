operator fun String.invoke(): String =
    rootProject.properties[this] as? String ?: error("Property $this not found")

repositories {
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
        content {
            includeGroup("me.lucko")
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
    implementation("com.squareup.retrofit2:converter-gson:${"retrofitVersion"()}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "gson")
    }
    implementation("com.charleskorn.kaml:kaml:${"kamlVersion"()}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    }
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
}
