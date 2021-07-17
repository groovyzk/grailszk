package org.zkoss.zk.grails.select

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.InvokerHelper
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Page
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zkplus.databind.DataBinder
import org.zkoss.zk.ui.Executions

/**
 * This class contains a set of jQuery-like methods for UI selection, manipulation amd data-binding.
 *
 * @author <a href="mailto:chanwit@gmail.com">Chanwit Kaewkasi</a>
 *
**/
class JQuery extends AbstractList<Component> {

    private static final Log LOG = LogFactory.getLog(JQuery.class)

    List<Component> components

    /**
     * Constructor
    **/
    JQuery() {
    }

    /**
     * Constructor that accepts a list of {@link org.zkoss.zk.ui.Component}.
     *
     * @param comps a list of ZK components.
     *
    **/
    JQuery(List<Component> comps) {
        if(comps.size() == 0) {
            throw new RuntimeException("Query returns nothing")
        }
        this.components = comps
    }

    /**
     * Bind an event handler to the event name.
     *
     * @param name name of the event to bind, in the lower case.
     *             For example, {@code 'click'} is for the {@link org.zkoss.zk.ui.event.Events#ON_CLICK} event.
     * @param c    event handler in the form of {@link groovy.lang.Closure} or {@link java.lang.String}.
     *             If it's a Closure then it will be a server-side handler.
     *             If it's a String then it will be a client-side handler,
     *             Client-side codes are required to write in JavaScript's syntax.
     *             Multiple-line Strings are acceopted.
     *
     * @return     {@code this} for chaining other calls.
     *
    **/
    def on(String name, c) {

        if(name=="ok") {
            name = name.toUpperCase()
        }

        def eventName = "on" + name.capitalize()

        if(c instanceof Closure)  {
            c.resolveStrategy = Closure.DELEGATE_FIRST
            components.each { comp ->
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

    /**
      * Retrieve the text from the first selected component.
      * If the component has {@code selectedItem} property,
      * it tries retrieve the text from the {@code selectedItem} first.
      *
      * @return if the component has {@code text} property, it returns the String value of {@code text}.<br/>
      *         otherwise if the component has {@code label} property, it returns the String value of {@code label}.<br/>
      *         otherwise if the component has {@code value} property, it returns the String value of {@code value}.<br/>
      *         The default return value is a blank String, not the {@code null} value.
    **/
    String text() {
        if(components?.size() == 0) {
            return ""
        }

        def comp = components?.get(0)
        if(comp) {
            if(comp.hasProperty('selectedItem')) {
                if(comp.selectedItem != null) {
                    comp = comp.selectedItem
                }
            }

            if(comp.hasProperty("text"))  return comp.text
            if(comp.hasProperty("label")) return comp.label
            if(comp.hasProperty("value")) return comp.value.toString()
        }

        return ""
    }

    /**
     * Set a String value to {@code text} property of every selected components.<br/>
     * This method looks for the {@code text} property first, if found the String value will be set to it.<br/>
     * Otherwise, this method will look for the {@code label} property.<br/>
     * Finally, it tries to set the String value to the {@code value} property.
     *
     * @param val a String to set to the {@code text} property.
     *
     * @return {@code this} to chain other calls.
     *
    **/
    def text(String val) {
        components?.each { comp ->
            if(comp) {
                if(comp.hasProperty("text"))  {
                    comp.text  = val
                } else if(comp.hasProperty("label")) {
                    comp.label = val
                } else if(comp.hasProperty("value")) {
                    try {
                        comp.value = val
                    } catch(ignored) {
                        throw new Exception("Component cannot be set a String to text, label or value")
                    }
                }
            }
        }

        return this
    }

    /**
     * Retrieve the {@code value} from the first selected component.<br/>
     * This method returns {@code null} if there is no selected component.
     *
     * @return if the component has {@code selectedItem} property,
     *         it will return the value of the selectedItem property first.
     *         otherwise, it will return the {@code value} of the component.<br/>
     *
    **/
    def val() {
        if(components?.size() == 0) {
            return null
        }

        def comp = components?.get(0)
        if(comp) {
            if(comp.hasProperty('selectedItem')) {
                if(comp.selectedItem != null) {
                    comp = comp.selectedItem
                }
            }

            return comp.value
        }

        return null
    }

    /**
     * Set the new value to the {@code value} property of every selected components.<br/>
     * There is a special behaviour of this method that if the new value is {@code null},
     * and a component has {@code selectedItem} property, the {@code null} value will be
     * set to the {@code selectedItem} property instead of setting {@code null} to the
     * {@code value} property of the component.
     *
     * @param value the new value.
     *
     * @return {@code this} for chaining the calls.
    **/
    def val(Object value) {
        components?.each { comp ->
            if(comp) {
                if(comp.hasProperty('selectedItem') && value==null) {
                    comp.selectedItem = null
                }/* else if(comp.hasProperty('rawValue')) {
                    comp.rawValue = value
                }*/ else {
                    comp.value = value
                }
            }
        }

        return this
    }


    /**
     * Focus to the first component.
     * If there is no component in the selected list, the method does nothing.
     *
     * @return {@code this} for chaining the calls.
     *
    **/
    def focus() {
        if(components?.size() == 0) {
            //
            // do nothing
            //
            return this
        }

        def comp = components?.get(0)
        if(comp) {
            comp.focus()
        }

        return this
    }

    /**
     *
     * @return the length of selected result.
     *
    **/
    def getLength() {
        return components?.size()
    }

    /**
     * Add style sheet classes to selected compoments.
     *
     * @return {@code this} for chaining the calls.
     *
    **/
    def addClass(String styleClasses) {
        if(!styleClasses) return
        components.each { comp ->
            if(comp.sclass == null || comp.sclass.size() == 0) {
                comp.sclass = styleClasses
            } else {
                comp.sclass = (comp.sclass + ' ' + styleClasses).trim()
            }
        }

        return this
    }

    /**
     * Remove style sheet classes out of selected compoments.
     *
     * @return {@code this} for chaining the calls.
     *
    **/
    def removeClass(String styleClasses) {
        if(!styleClasses) return
        def input = styleClasses.split(' ')
        components.each { comp ->
            def styles = comp.sclass?.split(' ')
            if(!styles) {
                styles = []
            }
            comp.sclass = (styles - input).join(' ')
        }

        return this
    }

    /**
     * Toggle style sheet classes for selected compoments.
     * If exists style sheet classes, this method removes the classes.
     * If not exists style sheet classes, this method adds the classes.
     *
     * @return {@code this} for chaining the calls.
     *
    **/
    def toggleClass(String styleClasses) {
        if(!styleClasses) return
        def input = styleClasses.split(' ')
        components.each { comp ->
            def styles = comp.sclass?.split(' ') as List
            if(!styles) {
                styles = []
            }
            input.each { inp ->
                if(inp in styles) {
                    styles.remove(inp)
                } else {
                    styles.add(inp)
                }
            }
            comp.sclass = styles.join(' ')
        }

        return this
    }

    /**
     * If components are enabled, change them to disable.
     * If components are disabled, change them to enable.
     *
     * @return {@code this} for chaining the calls.
     *
    **/
    def toggleEnable() {
        components?.each { comp ->
            comp.disabled = !(comp.disabled)
        }

        return this
    }

    /**
     * Retrieve the attribute {@code name} from the first component.
     *
     * @param name The attribute's name to retrieve.
     *
     * @return value of the first selected component.
     * If no compoment selected, {@code null} is returned.
    **/
    def attr(String name) {
        def comp = components?.get(0)
        if(comp) {
            return comp."$name"
        }

        return null
    }

    /**
     * Set the {@code value} to the attribute {@code name} for every selected compoments.
     *
     * @param name The attribute's name to retrieve.
     * @param value The new value to set to every selected compoments.
     *
     * @return {@code this} for chaining the calls.
    **/
    def attr(String name, def value) {
        components.each { comp ->
            comp."$name" = value
        }

        return this
    }

    /**
     * Use pair values in the {@code map} to set values
     * of attributes for every selected compoments.
     *
     * @param map a set of pair values in the form of {@code 'attr': value}
     *
     * @return {@code this} for chaining the calls.
    **/
    def attr(Map map) {
        map.each { k, v ->
            attr(k, v)
        }
        return this
    }

    /**
     * Set the return value of the closure {@code c}
     * to be the value of {@code name} attribute for every selected compoments.
     *
     * @param name the attribute's name to set.
     * @param c the closure to execute each time.
     * The return value is the new value to set to the selected compoments.
     *
     * @return {@code this} for chaining the calls.
    **/
    def attr(String name, Closure c) {
        components.eachWithIndex { comp, i ->
            def newVal = c.call(i, comp."$name")
            comp."$name" = newVal
        }
        return this
    }

    /**
     * @param index the index of the component to return.
     *
     * @return the compoment at index {@code inddex}.
    **/
    def getAt(int index) {
        return this.components[index]
    }

    /**
     * Accept [k: v] then loop over v with binding k to the closure.
     *
     * @param {@code map} a Map whose first element will be binding to each components.
     *
     * @return {@code this} for chaining the calls.
     */
    def each(Map map) {
        components?.each { comp ->
            comp.setAttribute('$JQ_BIND_EACH$', map, Component.COMPONENT_SCOPE)
        }
        return this
    }

    /**
     * Append the builder closure to the first selected compoment.
     *
     * @param c the builder closure.
     *
     * @return the newly created compoment by the builder.
     * If the selected component is {@code null}, this method returns {@code null}.
    **/
    def append(Closure c) {
        def comp = components?.get(0)
        if(comp) {
            def map = comp.removeAttribute('$JQ_BIND_EACH$', Component.COMPONENT_SCOPE)
            if(map) {
                def e = map.entrySet()[0]
                def k = e.key
                for(v in e.value) {
                    def cc = c.clone()
                    def bind = new Binding()
                    bind.setVariable(k, v)
                    cc.delegate = bind
                    comp.append(cc)
                }
                return null
            } else {
                return comp.append(c)
            }
        }
        return null
    }

    /**
     * Define a UI template closure for the selected compoments.
     * The template closure will be used by method {@code fill}.
     *
     * @param c the UI template closure to set.
     *
     * @return {@code this} for chaining the calls.
    **/
    def template(Closure c) {
        LOG.debug('Calling .template { }')
        components.each { comp ->
            LOG.debug('Setting $JQ_TEMPLATE$ for ' + comp)
            comp.setAttribute('$JQ_TEMPLATE$', c, Component.COMPONENT_SCOPE)
        }

        return this
    }

    /**
     * Remove the selected compoments from the parent,
     * so they will be garbage collected.
     *
     * @return {@code this} for chaining the calls.
    **/
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

    /**
     * @return height of the first selected component.
    **/
    def height() {
        def comp = components?.get(0)
        if(comp) {
            return comp.height
        }
        return null
    }

    /**
     * Show every selected component.
     *
     * @return self reference
    **/
    def show() {
        components?.each { comp ->
            comp?.visible = true
        }
        return this
    }

    /**
     * Bind the new object to the current binder.
     *
     * @param obj the new object to set to the current binder.
    **/
    def bind(obj) {
        def comp = components?.get(0)
        if (comp) {
            def binder = comp.getAttribute('$JQ_BINDER$', Component.COMPONENT_SCOPE)
            comp.setAttribute('$JQ_BINDING_OBJ$', obj,    Component.COMPONENT_SCOPE)
            binder.bindBean('root', obj)
            binder.loadAll()
        }
    }

    /**
     * Bind the current {@code obj} with the data-binding {@code map}.
     *
     * @param obj the current object to bind. The value {@code null} is to not bind.
     * @param map the data-binding map to use. It's in the form of {@code [property: selector]}.
     *
     * @return {@code this} for chaining the calls.
    **/
    def link(obj, map = null) {

        def comp = components?.get(0)
        if (comp) {

            LOG.debug "Binding $map"

            if(map == null) {
                def annotatedComps = Selectors.find(comp, '*').findAll {
                    Component c -> c.getWidgetAttribute('data-field') != null
                }
                map = [:]
                for(c in annotatedComps) {
                    map[c.getWidgetAttribute('data-field')] = c
                }
            }

            DataBinder binder = (DataBinder)comp.getAttribute('$JQ_BINDER$', Component.COMPONENT_SCOPE)
            if(!binder) {
                binder = new DataBinder()
                comp.setAttribute('$JQ_BINDER$', binder, Component.COMPONENT_SCOPE)
            }
            comp.setAttribute('$JQ_BINDING_OBJ$', obj, Component.COMPONENT_SCOPE)
            comp.setAttribute('$JQ_FILL_MAP$', map, Component.COMPONENT_SCOPE)

            LOG.debug "Set $comp attributes"

            map.each { expr, cid ->

                LOG.debug "Processing expr=$expr, cid=$cid"

                def selector = cid
                String attr = null
                def converter = null

                if(cid instanceof Map)  {
                    selector  = cid['selector']
                    attr      = cid['attr']
                    converter = cid['converter']
                }

                def selectedComponents = null
                if(selector instanceof String || selector instanceof GString) {
                    selectedComponents = Selectors.find(comp, preprocess(selector.toString()))
                } else if(selector instanceof Component) {
                    selectedComponents = [cid]
                }

                for(Component c in selectedComponents) {
                    if(c) {
                        c.setAttribute('$JQ_BINDER$', binder, Component.COMPONENT_SCOPE)
                        if(converter) {
                            c.setAttribute('$JQ_CONVERTER$', converter, Component.COMPONENT_SCOPE)
                            converter = "org.zkoss.zk.grails.select.JQConverter"
                        }
                        String bindingExpr = 'root.' + expr
                        if(attr) {
                            binder.addBinding(c, attr, bindingExpr) // auto
                        } else if(c.hasProperty('checked')) {
                            binder.addBinding(c, 'checked', bindingExpr, null, null, 'load', converter)
                            LOG.debug "Binding $c expr=$bindingExpr to='checked'"
                        } else if(c.hasProperty('selectedItem')) {
                            if(c.hasProperty('model') && c.model != null) {
                                binder.addBinding(c, 'selectedItem', bindingExpr, null, null, 'load', converter)
                                binder.addBinding(c, 'value',        bindingExpr, null, null, 'load', converter)
                                LOG.debug "Binding $c expr=$bindingExpr to='selectedItem' & 'value'"
                            } else {
                                // binder.addBinding(c, 'selectedItem', bindingExpr, null, null, 'both', converter)
                                binder.addBinding(c, 'value', bindingExpr, null, null, 'load', converter)
                                LOG.debug "Binding $c expr=$bindingExpr to='value' (case no model)"
                            }
                        } else if(c.hasProperty('label')) {
                            binder.addBinding(c, 'label', bindingExpr, null, null, 'load', converter)
                        } else {
                            binder.addBinding(c, 'value', bindingExpr, null, null, 'load', converter)
                        }

                    } else {
                        LOG.debug "Selection failed: $cid not found"
                    }
                }
            }

            if(obj){
                binder.bindBean('root', obj)
                binder.loadAll()
            }
        }

        return this
    }

    /**
     * Remove the current data-binding from the first selected compoment.
     *
     * @return {@code this} for chaining the calls.
    **/
    def unlink() {
        def comp = components?.get(0)
        if (comp) {
            def binder = comp.getAttribute('$JQ_BINDER$', Component.COMPONENT_SCOPE)
            def obj    = comp.removeAttribute('$JQ_BINDING_OBJ$', Component.COMPONENT_SCOPE)
            def map    = comp.removeAttribute('$JQ_FILL_MAP$', Component.COMPONENT_SCOPE)
            map?.each { expr, cid ->
                if(cid instanceof Map) {
                    cid = cid['selector']
                }
                for(c in Selectors.find(comp, preprocess(cid))) {
                    if(c) {
                        binder.bindBean('root', null)
                        binder.loadAll()
                        c.removeAttribute('$JQ_BINDER$', Component.COMPONENT_SCOPE)
                        binder.getBindings(c).each { b ->
                            binder.removeBinding(c, b.attr)
                        }
                    }
                }
            }
        }

        return this
    }

    /**
     * Disable selected components.
     *
     * @return {@code this} for chaining the calls.
    **/
    def disable() {
        components?.each { comp ->
            comp?.disabled = true
        }
        return this
    }

    /**
     * Enable selected components.
     *
     * @return {@code this} for chaining the calls.
    **/
    def enable() {
        components?.each { comp ->
            comp?.disabled = false
        }
        return this
    }

    /**
     * Handle method missing by:<br/>
     * 1. Looking for a prototype-based method, extended by calling {@code extend}.<br/>
     * 2. Looking for a method in the component's class.<br/>
     * 3. Map the method call the compoment property. For example, {@code visible()} maps to {@code visible}.<br/>
    **/
    def methodMissing(String name, args) {
        def comp = components?.get(0)
        if (comp) {
            //
            // lookup for a prototype-based method first
            //
            LOG.debug("Looking for method: ${name}")
            Closure meth = comp.getAttribute('$JQ_METHOD$_' + name, Component.COMPONENT_SCOPE)
            LOG.debug("Found method: ${meth.toString()}")
            if(meth != null) {
                try {
                    meth.delegate = comp
                    return InvokerHelper.invokeClosure(meth, args)
                } catch(e) {
                    LOG.debug(e.message)
                }
            }

            if(comp.respondsTo(name, args)) {
                return comp.metaClass.invokeMethod(comp, name, args)
            }

            if(comp.hasProperty(name)) {
                if(args.size() == 1) {
                    try {
                        comp."$name" = args[0]
                    } catch(e) { /* if cannot set value, do nothing */ }

                    return this
                } else if(args.size() == 0) {
                    return comp."$name"
                }
            }

        }

        throw new MissingMethodException(name, comp.class, args)
    }

    /**
     * Handle setting of the missing property
     *
     * @param name
     * @param arg
     */
    def propertyMissing(String name, arg) {
        components?.each { comp ->
            if(comp.hasProperty(name)) {
                comp."$name" = arg
            }
            else {
                throw new MissingPropertyException(name, comp.class)
            }
        }
    }

    /**
     * Handle getting of the missing property
     *
     * @param name
     * @return value of the property of the first selected component.
     */
    def propertyMissing(String name) {
        def comp = components?.get(0)
        if (comp) {
            return comp."$name"
        }

        throw new MissingPropertyException(name, comp)
    }

    /**
     * A Prototype object acts as a proxy for adding an ad-hoc method.
     *
     * @return an object of Prototype for setting an ad-hoc method for selected components.
     */
    Prototype getPrototype() {
        return new Prototype(components: components)
    }

    /**
     * Filter sibling nodes. Support only #id and tag's name.
     *
     * @return filtered sibling nodes.
    **/
    def siblings(String query=null) {
        def comp = components?.get(0)
        def siblings = (comp.parent.getChildren() as List) - comp
        if(!query) {
            if(comp) {
                return new JQuery(siblings)
            }
        } else {
            query = query.trim()
            if(query[0] == '#') {
                return new JQuery( siblings.findAll { it.id == query[1..-1] } )
            } else {
                return new JQuery( siblings.findAll { it.getDefinition().name == query } )
            }
        }
        return null
    }

    /**
     * @return the data-binder of the first selected component.
     */
    def binder() {
        def comp = components?.get(0)
        return comp?.getAttribute('$JQ_BINDER$', Component.COMPONENT_SCOPE)
    }

    def rawObject() {
        def comp = components?.get(0)
        def result = comp?.getAttribute('$JQ_BINDING_OBJ$', Component.COMPONENT_SCOPE)
        return result
    }

    /**
     *
      * @return a bound object and optionally save all data from component to the object
     */
    def object(opts = [:]) {
        //
        // Retrieve the bound entity
        //
        def comp = components?.get(0)
        def result = comp?.getAttribute('$JQ_BINDING_OBJ$', Component.COMPONENT_SCOPE)
        def map = comp?.getAttribute('$JQ_FILL_MAP$', Component.COMPONENT_SCOPE)

        if(result == null) {
            LOG.debug("object() not found, checking from selectedItem.");
            if(comp.hasProperty('selectedItem')) {
                map = comp?.selectedItem?.getAttribute('$JQ_FILL_MAP$', Component.COMPONENT_SCOPE)
                result = comp?.selectedItem?.getAttribute('$JQ_BINDING_OBJ$', Component.COMPONENT_SCOPE)
                comp = comp?.selectedItem
            }
        }

        if(result == null) {
            return null
        }


        if(opts['merge'] == true) {
            //
            // Work around Hibernate no session
            // re-attach to the current session
            // before proceeding
            //
            if(result.respondsTo('merge')) {
                result = result.merge()
            }
        }

        def mc = result.metaClass

        map.each { expr, cid ->

            LOG.debug "Binding expr=$expr, cid=$cid"

            def selector = cid
            def attr = null
            def converter = null
            if(cid instanceof Map)  {
                selector  = cid['selector']
                attr      = cid['attr']
                converter = cid['converter']
            }

            def selectedComponents = null
            if(selector instanceof String || selector instanceof GString) {
                selectedComponents = Selectors.find(comp, preprocess(selector.toString()))
            } else if(selector instanceof Component) {
                selectedComponents = [cid]
            }

            def objectToSet = result
            def exprToSet = expr
            def mp = null
            if(expr.contains('.')) {
                // it's going to be a complex prop
                for(sube in expr.split(/\./)) {
                    if(objectToSet.hasProperty(sube)) {
                        mp = objectToSet.hasProperty(sube)
                        exprToSet = sube
                        if(mp.type == String  ||
                           mp.type == Integer ||
                           mp.type == Long    ||
                           mp.type == Boolean ||
                           mp.type == Float   ||
                           mp.type == Double  ||
                           mp.type == Character ) {
                            // if it's a value field, stop the loop
                            // objectToSet cannot be the field, but the property's owner
                            break
                        }
                        objectToSet = objectToSet."${sube}"
                    } else {
                        break
                    }
                }
            } else {
                // a simple prop
                mp = objectToSet.hasProperty(expr)
            }
            if(exprToSet != 'id' && exprToSet != 'version') {
                for(c in selectedComponents) {
                    switch(mp.type) {
                        case String:
                            def propToGet = "value"
                            if(c.hasProperty('text'))       { propToGet = "text"  }
                            else if(c.hasProperty('label')) { propToGet = "label" }

                            if(propToGet != null) {
                                if(converter != null && converter instanceof Closure)
                                    objectToSet["$exprToSet"] = converter(c?."$propToGet")
                                else
                                    objectToSet["$exprToSet"] = c?."$propToGet"
                            }

                            break
                        case Boolean:
                            if(c.hasProperty('checked')) {
                                if(converter != null && converter instanceof Closure)
                                    objectToSet["$exprToSet"] = converter(c?.checked)
                                else
                                    objectToSet["$exprToSet"] = c?.checked
                            }
                            else {
                                if(converter != null && converter instanceof Closure)
                                    objectToSet["$exprToSet"] = converter(c?.value)
                                else
                                    objectToSet["$exprToSet"] = c?.value
                            }
                            break
                        case Integer:
                        case Double:
                        case Float:
                        case Long:
                            if(converter != null && converter instanceof Closure)
                                objectToSet["$exprToSet"] = converter(c?.value)
                            else
                                objectToSet["$exprToSet"] = c?.value
                            break
                        default:
                            if(converter != null && converter instanceof Closure)
                                objectToSet["$exprToSet"] = converter(c?.value)
                            else
                                objectToSet["$exprToSet"] = c?.value
                    }
                }
            }
        }

        return result
    }

    /**
     * Combine two query results
     *
     * @param jq another JQuery object
     * @return combined component from both query, wrapped by JQuery object
     */
    def plus(JQuery jq) {
        return new JQuery(this.components + jq.components)
    }

    def find(String q) {
        def result = []
        components.each { comp ->
            result += Selectors.find(comp, preprocess(q))
        }
        return new JQuery(result)
    }

    def extend(map) {
        map.each { name, arg ->
            components.each { comp ->
                comp.setAttribute('$JQ_METHOD$_' + name, arg, Component.COMPONENT_SCOPE)
            }
        }
        return this
    }

    /**
     * Fill every entry of {@code list} into the template, defined by {@code .template()}.
     *
     * @param list
     * @param ea an expression the object will be bound to
     * @param map the data-binding map, in the form of [field: selector]
     * @return self reference
     */
    def fill(list, ea = null, map = null) {
        LOG.debug("Calling .fill")
        def comp = components?.get(0)
        LOG.debug("First comp: $comp")
        try {
            Closure tmpl = comp.removeAttribute('$JQ_TEMPLATE$', Component.COMPONENT_SCOPE)
            LOG.debug("Got template: ${tmpl.toString()} from: $comp")
            if(tmpl != null) {
                LOG.debug("Data size: ${list.size()}")
                for(i in 1..list.size()) {
                    comp.append tmpl
                }
            }
        } catch(e) {
            LOG.debug(e.message)
        }

        // reuse each expression 'ea' and map,
        // so we can call .fill(list) for the 2nd time and so on.
        if(ea == null) {
            ea = comp.getAttribute('$JQ_FILL_EA$', Component.COMPONENT_SCOPE)
        } else {
            comp.setAttribute('$JQ_FILL_EA$',  ea,  Component.COMPONENT_SCOPE)
        }
        if(map == null) {
            map = comp.getAttribute('$JQ_FILL_MAP$', Component.COMPONENT_SCOPE)
        } else {
            comp.setAttribute('$JQ_FILL_MAP$', map, Component.COMPONENT_SCOPE)
        }

        def compList = Selectors.find(comp, preprocess(ea))
        LOG.debug("Linking points for [$ea] list: $compList")
        compList.each { c ->
            new JQuery([c]).unlink()
        }
        [list, compList].transpose().each {entity, c ->
            LOG.debug("Calling .link from .fill for entity:[$entity] component:[$c]")
            new JQuery([c]).link(entity, map)
        }

        return this
    }

    /**
     * Set model with multiple selection support. Work only with the first component in the selected result.
     *
     * @param model
     * @return self reference
     */
    def setModel(model) {
        if(components.size() == 0)
            return this

        def comp = components?.get(0)
        if(comp.hasProperty('model') && comp.hasProperty('multiple')) {
            model.setMultiple(comp.multiple)
            comp.model = model
            comp.invalidate()
        }

        return this
    }

    /**
     * Get parent of all selected components.
     *
     * @return JQuery wrapper for the parents
     */
    def parent() {
        return new JQuery(components.collect { c -> c.parent })
    }

    /**
     * Redirect method that is able to hold parameters across pages.
     *
     * @param args a Map that must contain at least {@code uri} and {@code params}
     * @return self reference
     */
    def redirect(Map args) {
        return redirect(args['uri'], args['params'])
    }

    /**
     * Redirect method that is able to hold parameters across pages.
     *
     * @param url
     * @param params
     * @return self reference
     */
    def redirect(url, Map params) {
        def comp = components?.get(0)
        if(comp) {
            comp.src = ""
            Executions.current.desktop.setAttribute('$JQ_REQUEST_PARAMS$', params)
            comp.src = url
        }

        return this
    }

    /**
     * This call method is to support the following syntax:<br/>
     * <pre>
     * {@code
     *   def paging = $('paging')
     *   paging pageSize: 10, totalSize: 100
     * }
     * </pre>
     *
     * @param args
     * @return
     */
    def call(args) {
        if(args instanceof Map) {
            components?.each { comp ->
                args.each {k, v ->
                    if(comp.hasProperty(k)) {
                        comp."$k" = v
                    }
                }
            }
        }

        return this
    }

    /**
     * Preprocess the query to support JQuery pseudo selectors.
     * Supported pseudo selectors are {@code eq} {@code lt} {@code gt} {@code even} {@code odd}.
     *
     * @param query a query to preprocess
     * @return pre-processed query
     */
    static String preprocess(query) {
        String result = query.replaceAll(/:(eq|lt|gt|even|odd)(\(.*?\))?/, { m, s, x ->
            if(x) { x = x[1..-2] }
            switch(s) {
                case "eq":   return ":nth-child(${x.toInteger()+1})"
                case "lt":   return ":nth-child(-n+${x})"
                case "gt":   return ":nth-child(n+${x})"
                case "even": return ":nth-child(2n)"
                case "odd":  return ":nth-child(2n+1)"
            }
            return m
        })
        LOG.debug("Preprocess Query: [$query] to [$result]")
        return result
    }

    /**
     * Selecting components based on args using {@code root} as the starting point.<br/>
     * <br/>
     * If args[0] is a String,  it will be used as the query.<br/>
     * The query will be pre-processed and passed to ZK's {@code Selectors.find}
     * If args[0] is a Component, it will be used as the only selected component.<br/>
     * If args[0] is a List, it will be used as the selected components. <br/>
     * If args[0] is a Closure, its {@code delegate} will be used as the only selected component. <br/>
     * If args[0] is an Event, its {@code target} will be used as the only selected component.
     *
     * @param root
     * @param args
     * @return
     */
    static JQuery select(root, Object[] args) {
        if(args.size() == 1) {
            Object arg0 = args[0]
            if(arg0 instanceof String) {
                return new JQuery(Selectors.find(root, preprocess(arg0)))
            } else if(arg0 instanceof Component) {
                return new JQuery([arg0])
            } else if(arg0 instanceof List) {
                return new JQuery((List<Component>)arg0)
            } else if(arg0 instanceof Closure) {
                return new JQuery([arg0.delegate])
            } else if(arg0 instanceof Event) {
                // handle $(it)
                return new JQuery([arg0.target])
            }
        } else if(args.size() == 2) {
            Object arg0 = args[0]
            Object arg1 = args[1]
            if(arg0 instanceof String && arg1 instanceof Page) {
                throw new RuntimeException("NYI")
            }
        }
        return null
    }

    @Override
    int size() {
        return components?.size()
    }

    @Override
    boolean isEmpty() {
        return components?.isEmpty()
    }


    @Override
    boolean contains(Object o) {
        return components?.contains(o)
    }

    @Override
    Iterator<Component> iterator() {
        return components?.iterator()
    }

    @Override
    Object[] toArray() {
        return components?.toArray()
    }

    @Override
    boolean add(Component e) {
        return components?.add(e)
    }

    @Override
    boolean remove(Object o) {
        return components?.remove(o)
    }

    @Override
    boolean containsAll(Collection<?> c) {
        return components?.containsAll(c)
    }

    @Override
    boolean addAll(Collection<? extends Component> c) {
        return components?.addAll(c)
    }

    @Override
    boolean addAll(int index, Collection<? extends Component> c) {
        return components?.addAll(index, c)
    }

    @Override
    boolean removeAll(Collection<?> c) {
        return components?.removeAll(c)
    }

    @Override
    boolean retainAll(Collection<?> c) {
        return components?.retainAll(c)
    }

    @Override
    void clear() {
        components?.clear()
    }

    @Override
    Component get(int index) {
        return components?.get(index)
    }

    @Override
    Component set(int index, Component element) {
        return components?.set(index, element)
    }

    @Override
    void add(int index, Component element) {
        components?.add(index, element)
    }

    @Override
    Component remove(int index) {
        return components?.remove(index)
    }

    @Override
    int indexOf(Object o) {
        return components?.indexOf(o)
    }

    @Override
    int lastIndexOf(Object o) {
        return components?.lastIndexOf(o)
    }

    @Override
    ListIterator<Component> listIterator() {
        return components?.listIterator()
    }

    @Override
    ListIterator<Component> listIterator(int index) {
        return components?.listIterator(index)
    }

    @Override
    List<Component> subList(int fromIndex, int toIndex) {
        return components?.subList(fromIndex, toIndex)
    }

}
