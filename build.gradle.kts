import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import java.net.InetAddress
import java.util.*

plugins {
    `java-library`
    `java-test-fixtures`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.palantir.git-version") version "0.13.0"
}

val productionBuild: Boolean? by project

val versionDetails: Closure<VersionDetails> by extra
val gitDetails = versionDetails()

fun boolProperty(name: String): Boolean {
    return ((properties[name] as String?) ?: "false").toBoolean()
}

val isMiCi = boolProperty("mi-ci")
val isRelease = boolProperty("mi-release")

val longTests: String? by project
val miCiStage = properties["mi-ci-stage"] as String?

group = "com.milaboratory"
version = if (version != "unspecified") version else "SNAPSHOT"
description = "MiLib"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()

    // Snapshot versions of redberry-pipe distributed via this repo
    maven {
        url = uri("https://pub.maven.milaboratory.com")
    }
}

val jacksonVersion = "2.13.1"
val redberryPipeVersion = "1.1.0"

dependencies {
    api("org.apache.commons:commons-math3:3.6.1")
    api("cc.redberry:pipe:$redberryPipeVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.apache.commons:commons-compress:1.20")
    implementation("commons-io:commons-io:2.7")
    implementation("org.lz4:lz4-java:1.8.0")
    implementation("com.beust:jcommander:1.72")
    implementation("info.picocli:picocli:4.1.2")
    implementation("net.sf.trove4j:trove4j:3.0.3")

    testFixturesImplementation("junit:junit:4.13.2")
    testFixturesImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-all:1.10.19")
}

val writeBuildProperties by tasks.registering(WriteProperties::class) {
    outputFile = file("${sourceSets.main.get().output.resourcesDir}/${project.name}-build.properties")
    property("version", version)
    property("name", "MiLib")
    property("revision", gitDetails.gitHash)
    property("branch", gitDetails.branchName ?: "")
    property("host", InetAddress.getLocalHost().hostName)
    property("production", productionBuild == true)
    property("timestamp", System.currentTimeMillis())
}

tasks.processResources {
    dependsOn(writeBuildProperties)
}

val miRepoAccessKeyId: String? by project
val miRepoSecretAccessKey: String? by project

publishing {
    repositories {
        if (miRepoAccessKeyId != null) {
            maven {
                name = "mipub"
                url = uri("s3://milaboratory-artefacts-public-files.s3.eu-central-1.amazonaws.com/maven")

                authentication {
                    credentials(AwsCredentials::class) {
                        accessKey = miRepoAccessKeyId!!
                        secretKey = miRepoSecretAccessKey!!
                    }
                }
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        from(components["java"])
        pom {
            withXml {
                asNode().apply {
                    appendNode("name", "MiLib")
                    appendNode(
                        "description",
                        "Yet another Java library for Next Generation Sequencing (NGS) data processing."
                    )
                    appendNode("url", "https://milaboratory.com/")
                }
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("dbolotin")
                    name.set("Dmitry Bolotin")
                    email.set("bolotin.dmitriy@gmail.com")
                }
                developer {
                    id.set("PoslavskySV")
                    name.set("Stanislav Poslavsky")
                    email.set("stvlpos@mail.ru")
                }
                developer {
                    id.set("mikesh")
                    name.set("Mikhail Shugay")
                    email.set("mikhail.shugay@gmail.com")
                }
            }
            scm {
                url.set("scm:git:https://github.com/milaboratory/milib")
            }
        }
    }
}

val signingKey: String? by project
if (signingKey != null) {
    signing {
        useInMemoryPgpKeys(
            Base64.getMimeDecoder().decode(signingKey).decodeToString(),
            ""
        )
        sign(publishing.publications["mavenJava"])
    }
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnit()
    minHeapSize = "1024m"
    maxHeapSize = "2048m"

    miCiStage?.let {
        if (it == "test") {
            systemProperty("longTests", "true");
        }
    }
    longTests?.let { systemProperty("longTests", it) }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

val checkPipeNotSnapshot by tasks.registering {
    doLast {
        if (redberryPipeVersion.contains('-'))
            throw GradleException("Can't publish to maven central with snapshot dependencies.")
    }
}

afterEvaluate {
    tasks.named("publishToSonatype") {
        dependsOn(checkPipeNotSnapshot)
    }
}
