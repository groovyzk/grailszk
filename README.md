# Grailszk

A plugin to integrate ZK infrastructure to modern Grails.
A port of [ZKGrails](https://github.com/zkgrails/zkgrails) plugin to version 4 of Grails.

## Migrating from ZKGrails 2.X.X

### Upgrading Grails

Follow these tutorials for upgrading to Grails 4

- https://docs.grails.org/3.0.x/guide/upgrading.html
- https://docs.grails.org/4.0.12/guide/single.html#upgrading
- https://www.danvega.dev/blog/2017/06/16/migrating-grails-2-x-applications-grails-3-x

## Requirements 

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

### Add grailszk dependency

build.gradle:
```groovy
dependencies {
    compile "io.github.zkgroovy:grailszk:$grailszkVersion"
}

configurations.all {
    exclude group: "org.slf4j", module: "slf4j-jdk14"
}
```

gradle.properties:

```
grailszkVersion=3.0.0
```

## Usage

- See [this simple Grailszk Example project](https://github.com/maiconandsilva/grailszk-example).
- See [Implementing Load-on-Demand using ZK and Grails](https://dzone.com/articles/implementing-load-demand-using)

### Versions below 2.5.2

Download ZK jars [`zkgrails2-common.jar`](https://github.com/zk-groovy/zkgrails-common.jar/blob/main/zkgrails2-common.jar)
and place them in the lib folder which must be inside the plugin root directory.  

### Notes

- Source code until version 2.5.2 comes from http://code.google.com/p/zkgrails.
- Below version 2.0 the code is forked from https://github.com/zkgrails/zkgrails.
