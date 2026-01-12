plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":config-http-message-converter"))
    implementation(project(":config-datasource"))
    implementation(project(":config-object-mapper"))
    implementation(project(":config-spring-docs"))
    implementation(project(":data-point"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")

    runtimeOnly("com.h2database:h2")

    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

//    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
//    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

//    implementation("ch.qos.logback:logback-classic:1.5.18")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}


