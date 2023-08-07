plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.onelitefeather.microtus:Minestom:1.1.0")
}

kotlin {
    jvmToolchain(17)
}
