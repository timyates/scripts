plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.0.1"
}

version = "0.1"
group = "io.micronaut.scripts.wip"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("info.picocli:picocli-codegen")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("info.picocli:picocli")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    implementation("io.micronaut:micronaut-http-client")

    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("org.apache.poi:poi-ooxml:5.2.0")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.8.2")
}


application {
    mainClass.set("io.micronaut.scripts.wip.WipCommand")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("io.micronaut.scripts.wip.*")
    }
}



