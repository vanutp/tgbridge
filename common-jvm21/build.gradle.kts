import org.jetbrains.kotlin.gradle.dsl.JvmTarget

dependencies {
    compileOnly(project(":common"))
    compileOnly("net.flectone.pulse:core:1.6.2")
    compileOnly("de.hexaoxi:carbonchat-api:3.0.0-beta.36")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks {
    withType<JavaCompile> {
        options.release = 21
    }
}
