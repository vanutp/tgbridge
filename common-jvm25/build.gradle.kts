import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("net.flectone.pulse:core:1.9.0")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks {
    withType<JavaCompile> {
        options.release = 25
    }
}
