import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("maven-publish")
    `java-gradle-plugin`
}

group = "com.russian-dude"
version = "1.5.1-1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.7.21")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
            version = "1.5.1-1"
            from(components["java"])
        }
    }
}