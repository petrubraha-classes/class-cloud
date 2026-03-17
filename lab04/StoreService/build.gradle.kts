plugins {
	java
	application
	id("org.springframework.boot") version "4.0.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.11.4"
}

group = "com.example.cloud"
version = "0.0.1-SNAPSHOT"
description = "Store Service project for Cloud Computing"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

application {
	mainClass = "com.example.cloud.storeservice.StoreServiceApplication"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
