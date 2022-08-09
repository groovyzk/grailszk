# Grailszk

A plugin to integrate ZK infrastructure to modern Grails.
A port of [ZKGrails](https://github.com/zkgrails/zkgrails) plugin to current versions of Grails.
Versions 5 and 4 are supported. Version 3 is allowed but not tested.

## Migrating from ZKGrails 2.X.X

### Upgrading Grails

Follow these tutorials for upgrading Grails:

- https://docs.grails.org/3.0.x/guide/upgrading.html
- https://docs.grails.org/4.0.12/guide/single.html#upgrading
- https://docs.grails.org/5.2.2/guide/upgrading.html#upgrading40x
- https://www.danvega.dev/blog/2017/06/16/migrating-grails-2-x-applications-grails-3-x

## Requirements 

---

#### Resources

All static resources in `src/main/resources/public` and `src/main/webapp` referenced in zul files as url
should be prefixed by **`/static/`**.

#### A properly configured `zk.xml` should be in `src/main/WEB-INF/`
```shell
grails-app install-zk-xml
```

#### Apply `grailszk-gradle-plugin` in `build.gradle`

```groovy
buildscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "io.github.zkgroovy:grailszk-gradle-plugin:1.0.0"
    }
}

apply plugin:"io.github.zkgroovy.grailszk-gradle-plugin"
```

### Add grailszk dependency (example)

build.gradle:
```groovy
// Omitted Grails dependencies for brevity
dependencies {
    implementation "io.github.zkgroovy:grailszk:$grailszkVersion"
    implementation "org.zkoss.zk:zk:$zkVersion", {
        exclude group: "org.zkoss.zk", module: "zkplus"
    }
    implementation "org.zkoss.zk:zul:$zkVersion"
    implementation "org.zkoss.zk:zhtml:$zkVersion"
    implementation "org.zkoss.zk:zkbind:$zkVersion"
    implementation "org.zkoss.common:zel:$zkVersion"
    implementation "org.zkoss.common:zcommon:$zkVersion"
    implementation ("org.zkoss.common:zweb:$zkVersion") {
        transitive = true
        exclude module: "ResourceCaches"
    }
    implementation "org.zkoss.zk:zkplus:7.0.1"
}

configurations.all {
    exclude group: "org.slf4j", module: "slf4j-jdk14"
}
```

gradle.properties (see available versions):

```properties
zkVersion=9.6.0.1
grailszkVersion=4.2.0
```

### Spring Security

In order to use this plugin with Spring Security you should have the following configuration:

build.gradle:
```groovy
repositories {
    maven { url "https://mavensync.zkoss.org/maven2" }
}

dependencies {
    implementation "org.zkoss.zk:zkspring-security:4.0.1"
}
```

zk.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<zk>
    <listener>
        <listener-class>org.zkoss.spring.init.SecurityContextAwareExecutionListener</listener-class>
    </listener>
</zk>
```

## Usage

- See [this simple Grailszk Example project](https://github.com/maiconandsilva/grailszk-example).

### Versions below 2.5.2

Download ZK jars [`zkgrails2-common.jar`](https://github.com/zk-groovy/zkgrails-common.jar/blob/main/zkgrails2-common.jar)
and place them in the lib folder which must be inside the plugin root directory.  

### Notes

> Always change the version of the project before publishing. e.g. publishing version 4 to Maven Local:
> `./change-version.sh -v 4 && ./gradlew -Pversion=4.2.2 publishToMavenLocal`

- Source code until version 2.5.2 comes from http://code.google.com/p/zkgrails.
- Below version 2.0 the code is forked from https://github.com/zkgrails/zkgrails.
