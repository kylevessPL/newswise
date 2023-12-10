rootProject.name = "newswise"

pluginManagement {
    val springBootVersion: String by settings
    val dependencyManagementVersion: String by settings
    val gradleVersionsVersion: String by settings
    val kotlinVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version dependencyManagementVersion
        id("com.github.ben-manes.versions") version gradleVersionsVersion
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
    }
}
