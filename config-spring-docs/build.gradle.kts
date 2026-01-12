plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation ("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springdoc:springdoc-openapi-ui:1.7.0")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}