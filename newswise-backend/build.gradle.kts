import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.github.ben-manes.versions")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "pl.piasta"
version = property("appVersion") as String

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    val coreNlpVersion: String by project

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.tukaani:xz")
    implementation("org.apache.commons:commons-compress")
    implementation("org.apache.opennlp:opennlp-tools")
    implementation("org.apache.opennlp:opennlp-dl")
    implementation("org.apache.tika:tika-core")
    implementation("org.apache.tika:tika-parser-pdf-module")
    implementation("org.apache.tika:tika-parser-html-module")
    implementation("org.apache.tika:tika-parser-html-commons")
    implementation("org.apache.tika:tika-parser-microsoft-module")
    implementation("org.apache.tika:tika-parser-miscoffice-module")
    implementation("org.apache.tika:tika-parser-text-module")
    implementation("org.apache.tika:tika-parser-image-module")
    implementation("org.apache.tika:tika-parser-ocr-module")
    implementation("edu.stanford.nlp:stanford-corenlp")
    implementation("edu.stanford.nlp:stanford-corenlp:$coreNlpVersion:models")
    implementation("io.github.pepperkit:corenlp-stop-words-annotator")
    implementation("nz.ac.waikato.cms.weka:weka-stable")
    implementation("org.bytedeco:tesseract-platform")
    implementation("org.languagetool:language-en")
    implementation("org.languagetool:hunspell-native-libs")
    implementation("com.github.pemistahl:lingua")
    implementation("io.github.cdimascio:openapi-spring-webflux-validator")

    configurations.all {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}

dependencyManagement {
    val linguaVersion: String by project
    val openApiSpringWebFluxValidatorVersion: String by project
    val stopWordsAnnotatorVersion: String by project
    val xzVersion: String by project
    val commonsCompressVersion: String by project
    val openNlpVersion: String by project
    val coreNlpVersion: String by project
    val wekaVersion: String by project
    val tesseractVersion: String by project
    val languageToolVersion: String by project
    val hunspellNativeLibsVersion: String by project
    val tikaVersion: String by project

    dependencies {
        dependency("org.tukaani:xz:$xzVersion")
        dependency("org.apache.commons:commons-compress:$commonsCompressVersion")
        dependency("org.apache.opennlp:opennlp-tools:$openNlpVersion")
        dependency("org.apache.opennlp:opennlp-dl:$openNlpVersion")
        dependency("edu.stanford.nlp:stanford-corenlp:$coreNlpVersion")
        dependency("io.github.pepperkit:corenlp-stop-words-annotator:$stopWordsAnnotatorVersion")
        dependency("nz.ac.waikato.cms.weka:weka-stable:$wekaVersion")
        dependency("org.bytedeco:tesseract-platform:$tesseractVersion")
        dependency("org.languagetool:language-en:$languageToolVersion")
        dependency("org.languagetool:hunspell-native-libs:$hunspellNativeLibsVersion")
        dependency("com.github.pemistahl:lingua:$linguaVersion")
        dependency("io.github.cdimascio:openapi-spring-webflux-validator:$openApiSpringWebFluxValidatorVersion")
        imports {
            mavenBom("org.apache.tika:tika-bom:$tikaVersion")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlinx.coroutines.FlowPreview,kotlinx.coroutines.DelicateCoroutinesApi,kotlinx.coroutines.ExperimentalCoroutinesApi,kotlin.io.path.ExperimentalPathApi"
        )
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
