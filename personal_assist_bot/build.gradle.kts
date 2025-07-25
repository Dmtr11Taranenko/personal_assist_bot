plugins {
	java
	id("org.springframework.boot") version "3.5.4-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.taranenko"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	compileOnly ("org.projectlombok:lombok:1.18.36")
	annotationProcessor ("org.projectlombok:lombok:1.18.36")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.telegram", "telegrambots", "6.5.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	mainClass.set("ru.taranenko.personal_assist_bot.PersonalAssistBotApplication")
}

tasks.bootJar {
	archiveFileName = "personal-assist-bot.jar"
}
