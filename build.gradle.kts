plugins {
    java
    application
    id("me.champeau.jmh") version "0.7.3"
}

group = "ru.centraluniversity"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Полезная нагрузка для собственного загрузчика собирается в отдельный source set
// `labs`: её классы НЕ попадают на runtime classpath приложения, поэтому обычный
// (родительский) загрузчик такой класс найти не сможет, а грузит его только
// CryptoClassLoader из зашифрованного файла.
sourceSets {
    create("labs") {
        java.srcDir("src/labs/java")
        compileClasspath += sourceSets.main.get().output
    }
}

application {
    mainClass.set("ru.centraluniversity.HomeworkDemo")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    jmhImplementation("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    jmhVersion.set("1.37")
    resultsFile.set(file("build/reports/jmh/results.json"))
    resultFormat.set("JSON")   // итоговые числа пишем в JSON, удобно для графиков/сводки
}

// Перед запуском демо и бенчмарков должен быть скомпилирован пейлоад.
tasks.named<JavaExec>("run") {
    dependsOn(tasks.named("labsClasses"))
}

tasks.named("jmh") {
    dependsOn(tasks.named("labsClasses"))
}
