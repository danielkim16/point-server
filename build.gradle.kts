plugins {
    id("org.springframework.boot") version "3.5.9" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    java
}

allprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }
}

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")

        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.projectlombok:lombok")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.mockk:mockk:1.14.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}