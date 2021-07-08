package org.zkoss.zk.grails.select

import java.util.List

import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.AbstractComponent
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.Page
import org.zkoss.zkplus.databind.DataBinder

public class JQuery {

    List<Component> components

    public JQuery() {
    }

    public JQuery(List<Component> comp) {
        this.components = comp;
    }

    def on(String name, c) {

        if(name=="ok") {
            name = name.toUpperCase()
        }

        def eventName = "on" + name.capitalize()

        if(c instanceof Closure)  {
            components.each { comp ->
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.delegate = comp
                def listener = new JQueryEventListener(handler: c)
                comp.addEventListener(eventName, listener)
            }
        } else if(c instanceof String) {
            components.each { comp ->
                comp.setWidgetListener(eventName, c.toString())
            }
        }
        return this
    }

    String text() {
        def comp = components?.get(0)
        if(comp) {
            if(comp.hasProperty("text"))  return comp.text
            if(comp.hasProperty("label")) return comp.label
            if(comp.hasProperty("value")) return comp.value
        }
        return ""
    }

    def text(Object val) {
        components?.each { comp ->
            if(comp) {
                if(comp.hasProperty("text"))  {
                    comp.text  = val
                } else if(comp.hasProperty("label")) {
                    comp.label = val
                } else if(comp.hasProperty("value")) {
                    comp.value = val
                }
            }
        }
        return this
    }

    def val() {
        def comp = components?.get(0)
        if(comp) {
            return comp.value
        }
        return null
    }

    def val(Object value) {
        components?.each { comp ->
            if(comp) {
                comp.value = value
            }
        }
        return this
    }

    def focus() {
        def comp = components?.get(0)
        if(comp) {
            comp.focus()
        }
        return this
    }

    def size() {
        return components?.size()
    }

    def getLength() {
        return components?.size()
    }

    def addClass(String styleClass) {
        if(!styleClass) return
        components.each { comp ->
            if(comp.sclass == null || comp.sclass.size() == 0) {
                comp.sclass = styleClass
            } else {
                comp.sclass = (comp.sclass + ' ' + styleClass).trim()
            }
        }
        return this
    }

    def removeClass(String styleClass) {
        if(!styleClass) return
        def inp = styleClass.split(' ')
        components.each { comp ->
            def styles = comp.sclass?.split(' ')
            if(!styles) {
                styles = []
            }
            comp.sclass = (styles - inp).join(' ')
        }
        return this
    }

    def attr(String name) {
        def comp = components?.get(0)
        if(comp) {
            return comp."$name"
        }
        return null
    }

    def attr(String name, def value) {
        components.each { comp ->
            comp."$name" = value
        }
        return this
    }

    def attr(Map map) {
        map.each { k, v ->
            attr(k, v)
        }
        return this
    }

    def attr(String name, Closure c) {
        components.eachWithIndex { comp, i ->
            def newVal = c.call(i, comp."$name")
            if(newVal) {
                comp."$name" = newVal
            }
        }
        return this
    }

    def getAt(int index) {
        return this.components[index]
    }

    def append(Closure c) {
        def comp = components?.get(0)
        if(comp) {
            return comp.append(c)
        }
        return null
    }

    JQuery detach() {
        components.each { comp ->
            comp.detach()
        }
        return this
    }

    def asType(Class clazz) {
        def obj = null
        switch(clazz) {
            case Component:
                obj = this.components[0]
                break
            case String:
                obj = this.toString()
                break
            case List:
                obj = this.components
                break
        }
        return obj
    }

    def height() {
        def comp = components?.get(0)
        if(comp) {
            return comp.height
        }
        return null
    }

    def show() {
        components.each { comp ->
            comp.visible = true
        }
        return this
    }

    def link(obj, map) {

        if(obj == null) return

        def comp = components?.get(0)
        if (comp) {
            def binder = new DataBinder()
            comp.setAttribute('$JQ_BINDER$', binder, Component.COMPONENT_SCOPE)
            map.each { expr, cid ->
                def c = Selectors.find(comp, cid)[0]
                if(c) {
                    binder.addBinding(c, 'value', 'root.' + expr)
                } else {
                    // LOG for warning
                }
            }
            binder.bindBean('root', obj)
            binder.loadAll()
        }

    }

    def unlink() {
        def comp = components?.get(0)
        if (comp) {
            comp.removeAttribute('$JQ_BINDER$', Component.COMPONENT_SCOPE)
        }
    }

    static JQuery select(root, Object[] args) {
        if(args.length == 1) {
            Object arg0 = args[0];
            if(arg0 instanceof String) {
                return new JQuery(Selectors.find(root, (String)arg0));
            } else if(arg0 instanceof Component) {
                ArrayList<Component> comps = new ArrayList<Component>();
                comps.add((Component)arg0);
                return new JQuery(comps);
            } else if(arg0 instanceof List) {
                return new JQuery((List<Component>)arg0);
            }
        } else if(args.length == 2) {
            Object arg0 = args[0];
            Object arg1 = args[1];
            if(arg0 instanceof String && arg1 instanceof Page) {
                throw new RuntimeException("NYI");
            }
        }
        return null;
    }

}
