plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

    // JDBI
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // Spring Security
    implementation("org.springframework.security:spring-security-core:6.3.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        val user = System.getenv("DB_USER")
        val password = System.getenv("DB_PASSWORD")
        environment("DB_URL", "jdbc:postgresql://localhost:5432/postgres?user=$user&password=$password")
    }
    dependsOn("waitForDB")
    finalizedBy("stopDB")
}

task<Exec>("startDB") {
    commandLine("docker", "compose", "up", "-d", "--build", "--force-recreate", "db-postgres")
}

task<Exec>("waitForDB") {
    commandLine("docker", "exec", "db-postgres", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("startDB")
}

task<Exec>("stopDB") {
    commandLine("docker", "compose", "down")
}

tasks.named("check") {
    dependsOn("waitForDB")
    finalizedBy("stopDB")
}
