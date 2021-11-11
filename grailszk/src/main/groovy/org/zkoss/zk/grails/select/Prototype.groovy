package org.zkoss.zk.grails.select

import org.zkoss.zk.ui.Component

class Prototype {

    def components

    def propertyMissing(String name, val) {
        components?.each { comp ->
            comp.setAttribute('$JQ_METHOD$_' + name, val, Component.COMPONENT_SCOPE)
        }
    }

}