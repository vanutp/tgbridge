import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

plugins {
	id("dev.architectury.loom")
}

val minecraftVersion: String by project
val yarnMappings: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project
val adventureFabricVersion: String by project

dependencies {
	minecraft("com.mojang:minecraft:${minecraftVersion}")
//	mappings (loom.officialMojangMappings())
	mappings("net.fabricmc:yarn:${yarnMappings}:v2")
	modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

	implementation(project(":common"))
	shadow(project(":common"))

	// PlaceholderAPI
	modImplementation("eu.pb4:placeholder-api:2.4.1+1.21")

	// Styled Chat
	modImplementation("maven.modrinth:styled-chat:2.6.0+1.21")
	modImplementation("eu.pb4:player-data-api:0.6.0+1.21")
	modImplementation("me.lucko:fabric-permissions-api:0.3.1")
	modImplementation("eu.pb4:predicate-api:0.5.2+1.21")
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
		destinationDirectory.set(file("../build/release"))
	}
}

modrinth {
	uploadFile.set(tasks.remapJar)
	gameVersions.addAll("1.20.6", "1.21", "1.21.1")
	loaders.add("fabric")
	dependencies {
		required.project("fabric-api")
		required.project("fabric-language-kotlin")
	}
}
