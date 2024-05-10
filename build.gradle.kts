plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.jett.jp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))

    dependencies{
//        implementation("com.android.tools.build:android-sdk-platform-tools:33.0.2")
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

//        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    findByName("buildPlugin")?.apply {

    }?.doLast {
         println("xxxxxxx ${outputs.files.files}")

        outputs.files.files.forEach {
            val to = File("${project.projectDir.absolutePath}/releases/${it.name}")
            to.delete()
            it.copyTo(to)
        }
    }
}
