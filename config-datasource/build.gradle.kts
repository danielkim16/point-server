plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // JPA & H2
    implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly ("com.h2database:h2")

    // Querydsl
    implementation ("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor ("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor ("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor ("jakarta.persistence:jakarta.persistence-api")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}