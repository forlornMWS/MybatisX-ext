import org.gradle.internal.classpath.Instrumented.systemProperty

buildscript {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://dl.bintray.com/jetbrains/intellij-plugin-service") }
        maven { url = uri("https://dl.bintray.com/jetbrains/intellij-third-party-dependencies/") }
    }
    dependencies {
        classpath("org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
    }
}

plugins {
    java
    kotlin("jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

intellijPlatform {

    pluginConfiguration {
        name = "MybatisX-ext"
    }
    sandboxContainer.set(project.layout.projectDirectory.dir("${rootProject.rootDir}/idea-sandbox"))
}

group = "com.baomidou.plugin.idea.ext.mybatisx"
version = "1.0.4"

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform{
        defaultRepositories()

    }
}

dependencies {
    intellijPlatform{
//        create("IU", "2025.2")
        local("D:\\Program\\JetBrains\\IntelliJ IDEA Ultimate")
        bundledPlugins(listOf("com.intellij.java", "org.jetbrains.kotlin", "com.intellij.database", "com.intellij.spring", "com.intellij.spring.boot",  "com.intellij.modules.ultimate"))
        testBundledModule("com.intellij.modules.ultimate")
        // Add the ultimate module capability
        bundledModule("com.intellij.modules.ultimate")
    }
    implementation("com.softwareloop:mybatis-generator-lombok-plugin:1.0")
    implementation("uk.com.robust-it:cloning:1.9.2")
    implementation("org.mybatis.generator:mybatis-generator-core:1.4.0")
    implementation("org.freemarker:freemarker:2.3.30")
    implementation("com.itranswarp:compiler:1.0")
    testImplementation("junit:junit:4.13.1")
    testImplementation("commons-io:commons-io:2.18.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

intellijPlatformTesting {
    runIde {
        systemProperty("idea.is.internal", "true")
        systemProperty("idea.debug.mode", "true")
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    patchPluginXml {
        sinceBuild.set("232")
    }
}
