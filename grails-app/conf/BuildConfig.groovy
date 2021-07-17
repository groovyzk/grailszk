grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

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
        test ("com.h2database:h2:1.3.168") {
            export = false
        }
        test("org.gebish:geb-junit4:0.9.0") {
            export = false
        }
        test("org.seleniumhq.selenium:selenium-support:2.32.0"){
            export = false
        }
        test("org.seleniumhq.selenium:selenium-firefox-driver:2.32.0"){
            export = false
        }
    }

    plugins {
        test (":hibernate:$grailsVersion") {
            export = false
        }
        build(":tomcat:$grailsVersion") {
            export = false
        }
        build(":release:2.2.0") {
            export = false
        }
        runtime(":resources:1.1.6") {
            export = false
        }
        test(":geb:0.9.0") {
            export = false
        }
    }
}
