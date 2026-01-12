plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":config-object-mapper"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}