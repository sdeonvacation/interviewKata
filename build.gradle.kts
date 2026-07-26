plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "dev.interviewkata"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")

    // Spring AI
    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-M4"))
    implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    // YAML parsing
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Utilities
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // docker-java's default negotiated API version (1.32) is rejected by modern Docker engines
    // (colima 29.x requires >= 1.40). Pin a compatible version for Testcontainers-backed tests.
    // Test workers do NOT inherit the Gradle process' system properties, so it must be set here.
    // Override on the command line with -Dapi.version=... if needed.
    systemProperty("api.version", System.getProperty("api.version", "1.43"))
    // DOCKER_HOST and TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE are inherited from the environment
    // automatically; pass them through explicitly so a non-inheriting runner still works.
    System.getenv("DOCKER_HOST")?.let { environment("DOCKER_HOST", it) }
    System.getenv("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE")
        ?.let { environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", it) }
}
