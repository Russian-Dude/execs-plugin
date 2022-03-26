import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("maven-publish")
    `java-gradle-plugin`
    id ("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.russian-dude"
version = "1.3.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.31")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
}

val jar: Jar by tasks

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    isZip64 = true
}

jar.apply {
    from(configurations.compileOnly.fileCollection().forEach {
        zipTree(it)
        exclude ("META-INF/MANIFEST.MF")
        exclude ("META-INF/*.SF")
        exclude ("META-INF/*.DSA")
        exclude ("META-INF/*.RSA")
    })
}

gradlePlugin {
    plugins {
        create("com.russian-dude.execs-plugin") {
            id = "com.russian-dude.execs-plugin"
            implementationClass = "com.rdude.exECS.plugin.ExEcsGradlePlugin"
        }
    }
}

