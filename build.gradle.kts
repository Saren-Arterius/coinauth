import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


group = "net.wtako"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.50")
        classpath("com.github.jengelman.gradle.plugins:shadow:2.0.4")
    }
}


plugins {
    java
    kotlin(module="jvm") version "1.2.50"
}

repositories {
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenCentral()
    jcenter()
}

apply {
    plugin("com.github.johnrengelman.shadow")
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    compileOnly("org.bukkit:bukkit:1.12.2-R0.1-SNAPSHOT") // The Bukkit API with no shadowing.
    implementation("com.sxtanna.database:Kedis:1.1")
    implementation("com.squareup.moshi:moshi:1.6.0")
    implementation("com.squareup.moshi:moshi-adapters:1.6.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.6.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}