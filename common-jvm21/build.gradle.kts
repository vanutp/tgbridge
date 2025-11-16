import org.jetbrains.kotlin.gradle.dsl.JvmTarget

dependencies {
    compileOnly(project(":common"))
    compileOnly("net.flectone.pulse:core:1.6.2")
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
