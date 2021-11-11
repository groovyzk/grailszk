package ${packageName}

import org.zkoss.zk.grails.*

class ${className} {
    static config = {
        model    "page" // or "list"
        domain   ${domainName}
        pageSize 20
        sorted   true
    }
}
