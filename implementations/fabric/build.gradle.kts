import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

plugins {
	id("dev.architectury.loom")
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project
val vanishVersion: String by project

repositories {
	maven("https://api.modrinth.com/maven")
}

dependencies {
	minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

	api(project(":common"))
	shadow(project(":common"))
    implementation(project(":common-jvm21"))
	shadow(project(":common-jvm21"))

	modCompileOnly("maven.modrinth:vanish:$vanishVersion")
}

loom {
	accessWidenerPath = file("src/main/resources/tgbridge.accesswidener")
}

tasks {
	named<ProcessResources>("processResources") {
		inputs.property("version", project.version)

		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}

	named<Jar>("jar") {
		enabled = false
	}

	named<ShadowJar>("shadowJar") {
		dependsOn("processResources")
		finalizedBy("remapJar")

		from(sourceSets.main.get().output.classesDirs)
		from(sourceSets.main.get().output.resourcesDir)

		configurations = listOf(project.configurations.shadow.get())
		archiveClassifier = jar.get().archiveClassifier
		destinationDirectory = jar.get().destinationDirectory
	}

	named<RemapJarTask>("remapJar") {
		inputFile = shadowJar.get().archiveFile
		archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
		destinationDirectory.set(rootProject.layout.buildDirectory.dir("release"))
	}
}

modrinth {
	uploadFile.set(tasks.remapJar)
	gameVersions.addAll(
		"1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
		"1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4",
		"1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10"
	)
	loaders.add("fabric")
	dependencies {
		required.project("fabric-api")
		required.project("fabric-language-kotlin")
	}
}
