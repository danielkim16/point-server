plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}