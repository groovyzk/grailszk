package org.zkoss.zk.grails.select

class JQueryExtender {

    def extend(map) {
        map.each { name, closure ->
            JQuery.metaClass."$name" = closure
        }
    }

}