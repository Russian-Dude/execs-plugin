import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("maven-publish")
    `java-gradle-plugin`
}

group = "com.russian-dude"
version = "1.5.0-k1.6.21-1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.21")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

gradlePlugin {
    plugins {
        create("com.russian-dude.execs-plugin") {
            id = "execs-plugin"
            implementationClass = "com.rdude.exECS.plugin.ExEcsGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.russian-dude"
            artifactId = "execs-plugin"
            version = "1.5.0-k1.6.21-1"
            from(components["java"])
        }
    }
}