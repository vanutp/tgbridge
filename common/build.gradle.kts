dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.10.0-SNAPSHOT") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-serialization-core")
        exclude(module = "kotlinx-serialization-json")
    }
    implementation("com.squareup.retrofit2:converter-gson:2.10.0-SNAPSHOT") {
        exclude(module = "gson")
    }
    implementation("com.charleskorn.kaml:kaml:0.56.0") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlinx-serialization-core")
    }
    compileOnly("com.google.code.gson:gson:2.10.1")
}
