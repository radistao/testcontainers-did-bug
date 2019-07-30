import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    `java-library`
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.testcontainers:junit-jupiter:1.11.4")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.26")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED, STARTED)
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
