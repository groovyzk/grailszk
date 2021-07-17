package org.zkoss.zk.grails.select

import org.zkoss.zk.ui.Component
import org.zkoss.zkplus.databind.TypeConverter

class JQConverter implements TypeConverter, java.io.Serializable {

    public Object coerceToUi(Object val, Component comp) {
        return val
    }

    public Object coerceToBean(Object val, Component comp) {
        def converter = comp.getAttribute('$JQ_CONVERTER$', Component.COMPONENT_SCOPE)
        if(converter) {
            return converter.call(val)
        }

        return val
    }

}