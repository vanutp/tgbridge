dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-serialization-core")
        exclude(module = "kotlinx-serialization-json")
    }
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") {
        exclude(module = "gson")
    }
    implementation("com.charleskorn.kaml:kaml:${rootProject.properties["kamlVersion"]}") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    }
    compileOnly("com.google.code.gson:gson:2.10.1")
}
