grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

/*
grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]
*/

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenCentral()
        mavenLocal()
        mavenRepo "http://repo1.maven.org/maven2"
    }
    dependencies {
        build ("net.java.dev.inflector:inflector:0.7.0")
        build ("com.google.code.maven-svn-wagon:maven-svn-wagon:1.4") {
            export = false
        }
        build ("org.apache.maven.wagon:wagon-webdav-jackrabbit:2.2") {
            export = false
        }
        test ("com.h2database:h2:1.3.172") {
            export = false
        }
        test("org.gebish:geb-junit4:0.9.2") {
            export = false
        }
        test("org.seleniumhq.selenium:selenium-support:2.41.0"){
            export = false
        }
        test("org.seleniumhq.selenium:selenium-firefox-driver:2.41.0"){
            export = false
        }
    }

    plugins {
        build(":tomcat:7.0.50") {
            export = false
        }
        runtime(":hibernate:3.6.10.7") {
            export = false
        }
        build(":release:3.0.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
        /*runtime(":asset-pipeline:1.8.7") {
            export = false
        }*/
        runtime(":resources:1.2.8") {
            export = false
        }
        test(":geb:0.9.0") {
            export = false
        }
    }
}
