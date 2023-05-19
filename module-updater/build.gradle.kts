plugins {
    id("groovy") 
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.9"
}

version = "0.1"
group = "io.micronaut.scripts"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.groovy:micronaut-runtime-groovy")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("jakarta.annotation:jakarta.annotation-api")
    compileOnly("info.picocli:picocli-codegen")
    runtimeOnly("ch.qos.logback:logback-classic")
    testImplementation("io.micronaut:micronaut-http-client")

}

application {
    mainClass.set("io.micronaut.scripts.ModuleUpdaterCommand")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

micronaut {
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("io.micronaut.scripts.*")
    }
}



