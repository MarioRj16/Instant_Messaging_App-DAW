import org.springframework.boot.gradle.tasks.run.BootRun

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

    // SpringDoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // JDBI
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    // Kotlinx DateTime
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

val dockerImageTagJvm = "chimp"
val dockerImageTagNginx = "nginx"
val dockerImageTagPostgres = "db-postgres"
val dockerImageTagUbuntu = "ubuntu"

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
    commandLine("docker", "compose", "up", "-d", dockerImageTagPostgres)
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

tasks.named<BootRun>("bootRun") {
    dependsOn("startDB")
    finalizedBy("stopDB")
}

// Docker related tasks

task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree(layout.buildDirectory.file("libs/${rootProject.name}-$version.jar").get().toString()))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

task<Exec>("buildImageJvm") {
    dependsOn("extractUberJar")
    commandLine("docker", "build", "-t", dockerImageTagJvm, "-f", "docker/Dockerfile-chimp", ".")
}
task<Exec>("buildImageNginx") {
    commandLine("docker", "build", "-t", dockerImageTagNginx, "-f", "docker/nginx/Dockerfile-nginx", ".")
}
task<Exec>("buildImagePostgres") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImageTagPostgres,
        "-f",
        "docker/postgres/Dockerfile-postgres",
        "docker/postgres",
    )
}
task<Exec>("buildImageUbuntu") {
    commandLine("docker", "build", "-t", dockerImageTagUbuntu, "-f", "docker/Dockerfile-ubuntu", ".")
}
task("buildImageAll") {
    dependsOn("buildImageJvm")
    dependsOn("buildImageNginx")
    dependsOn("buildImagePostgres")
    dependsOn("buildImageUbuntu")
}
task<Exec>("composeUp") {
    commandLine("docker", "compose", "up", "--build", "--force-recreate", "--scale", "chimp=3")
    dependsOn("extractUberJar")
}
task<Exec>("composeDown") {
    commandLine("docker", "compose", "down")
}
