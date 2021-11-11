package ${packageName}

import org.zkoss.zk.grails.*

class ${className} {

    ${domainName} selected

    List<${domainName}> get${domainName}() {
        ${domainName}.list()
    }
}
