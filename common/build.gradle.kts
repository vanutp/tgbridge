operator fun String.invoke(): String =
    rootProject.properties[this] as? String ?: error("Property $this not found")

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
}
