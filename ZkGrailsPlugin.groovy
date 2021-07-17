import grails.util.Environment
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.springframework.core.io.FileSystemResource
import org.zkoss.zk.grails.ZkBuilder
import org.zkoss.zk.grails.ZkConfigHelper
import org.zkoss.zk.grails.artefacts.*
import org.zkoss.zk.grails.composer.GrailsBindComposer
import org.zkoss.zk.grails.composer.JQueryComposer
import org.zkoss.zk.grails.dev.DevHolder
import org.zkoss.zk.grails.extender.ListboxExtender
import org.zkoss.zk.grails.livemodels.LiveModelBuilder
import org.zkoss.zk.grails.livemodels.SortingPagingListModel
import org.zkoss.zk.grails.select.JQuery
import org.zkoss.zk.grails.web.ComposerMapping
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.lang.Library

class ZkGrailsPlugin {
    // the plugin version
    def version = "2.4.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > 2.3.8"

    def loadAfter = ['core', 'controllers']

    def artefacts = [
        CometArtefactHandler,
        ComposerArtefactHandler,
        FacadeArtefactHandler,
        LiveModelArtefactHandler,
        ViewModelArtefactHandler,
    ]

    def watchedResources = ["file:./grails-app/composers/**/*Composer.groovy",
                            "file:./plugins/*/grails-app/composers/**/*Composer.groovy",
                            "file:./grails-app/comets/**/*Comet.groovy",
                            "file:./plugins/*/grails-app/comets/**/*Comet.groovy",
                            "file:./grails-app/facade/**/*Facade.groovy",
                            "file:./plugins/*/grails-app/facade/**/*Facade.groovy",
                            "file:./grails-app/livemodels/**/*LiveModel.groovy",
                            "file:./plugins/*/grails-app/livemodels/**/*LiveModel.groovy",
                            "file:./grails-app/viewmodels/**/*ViewModel.groovy",
                            "file:./plugins/*/grails-app/viewmodels/**/*ViewModel.groovy",

                            //
                            // support watching ZUL files
                            //
                            "file:./grails-app/zul/**/*.zul"
                            ]

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/conf/Config.groovy",
        "grails-app/conf/TestUrlMappings.groovy",
        "grails-app/domain/zk/**",
        "grails-app/services/zk/**",
        "grails-app/comets/**",
        "grails-app/controllers/zk/**",
        "grails-app/composers/**",
        "grails-app/facade/**",
        "grails-app/livemodels/**",
        "grails-app/views/**",
        "grails-app/viewmodels/**",
        "grails-app/taglib/MyTagLib.groovy",
        "grails-app/i18n/*.properties",
        "grails-app/zul/**",
        "web-app/css/**",
        "web-app/issue*",
        "web-app/META-INF/**",
        "web-app/test/**",
        "web-app/WEB-INF/**",
        "web-app/ext/images/skin/**",
        "web-app/ext/images/*.ico",
        // "web-app/ext/images/grails_*",
        "web-app/ext/images/leftnav_*",
        "web-app/ext/images/sp*",
        "web-app/**/*.zul",
        "test/**",
        "src/docs/**",
        "src/java/org/zkoss/zk/grails/test/**"
    ]

    def author = "Chanwit Kaewkasi"
    def authorEmail = "chanwit@gmail.com"
    def title = "ZK plugin for Grails"
    def description = '''
Originated from Flyisland\'s ZK Plugin,
ZKGrails adds and enhances the ZK\'s RIA capabilities
and seamlessly integrates them with Grails\' infrastructures.
'''

    def license = "LGPL"

    def documentation = "http://grails.org/plugin/zk"

    private String getScope(clazz, String defaultScope) {
        String beanScope = GCU.getStaticPropertyValue(clazz, "scope") as String
        if(beanScope == null) {
            beanScope = defaultScope
        }
        return beanScope
    }

    def doWithSpring = {

        Library.setProperty("org.zkoss.web.servlet.http.URLEncoder", "org.zkoss.zk.grails.web.URLEncoder")

        if(Environment.current == Environment.DEVELOPMENT) {
            devHolder(org.zkoss.zk.grails.dev.DevHolder) { bean ->
                bean.scope = "singleton"
            }
        }

        //
        // Registering new scopes
        //
        desktopScope(org.zkoss.zk.grails.scope.DesktopScope)
        pageScope   (org.zkoss.zk.grails.scope.PageScope   )
        zkgrailsScopesConfigurer(org.springframework.beans.factory.config.CustomScopeConfigurer) {
            scopes = ['desktop': ref('desktopScope'),
                      'page'   : ref('pageScope')   ]
        }

        // Registering desktopCounter
        desktopCounter(org.zkoss.zk.grails.DesktopCounter) { bean ->
            bean.scope = "singleton"
            bean.autowire = "byName"
        }

        //
        // Registering ViewModel Beans to support MVVM
        //
        application.viewModelClasses.each { viewModelClass ->
            "${viewModelClass.propertyName}"(viewModelClass.clazz) { bean ->
                bean.scope = this.getScope(viewModelClass.clazz, "prototype")
                bean.autowire = "byName"
            }
        }

        //
        // Registering 'GrailsBindComposer'
        //
        "grailsBindComposer"(GrailsBindComposer.class) { bean ->
            bean.scope = 'prototype'
            bean.autowire = 'byName'
        }

        //
        // Registering Composer Beans
        //
        application.composerClasses.each { composerClass ->
            def composerBeanName = composerClass.propertyName
            if(composerClass.packageName) {
                composerBeanName = "${composerClass.packageName}.${composerBeanName}"
            }
            def clazz = composerClass.clazz
            if(clazz.superclass == Script.class) {
                "${composerBeanName}"(JQueryComposer.class) { bean ->
                    bean.scope = "prototype"
                    bean.autowire = "byName"
                    innerComposer = clazz
                }
            } else {
                "${composerBeanName}"(composerClass.clazz) { bean ->
                    bean.scope = this.getScope(composerClass.clazz, "prototype")
                    bean.autowire = "byName"
                }
            }
        }

        //
        // Registering Facade Beans
        //
        application.facadeClasses.each { facadeClass ->
            "${facadeClass.propertyName}"(facadeClass.clazz) { bean ->
                bean.scope = this.getScope(facadeClass.clazz, "session")
                bean.autowire = "byName"
            }
        }

        //
        // Registering Comet classes
        //
        application.cometClasses.each { cometClass ->
            "${cometClass.propertyName}"(cometClass.clazz) { bean ->
                bean.scope = this.getScope(cometClass.clazz, "prototype")
                bean.autowire = "byName"
            }
        }

        //
        // Registering UI-Model classes
        //
        application.liveModelClasses.each { modelClass ->
            def cfg = GCU.getStaticPropertyValue(modelClass.clazz, "config")
            if(cfg) {
                def lmb = new LiveModelBuilder()
                cfg.delegate = lmb
                cfg.resolveStrategy = Closure.DELEGATE_ONLY
                cfg.call()
                if (lmb.map['model'] == 'page') {
                    "${modelClass.propertyName}"(SortingPagingListModel.class) { bean ->
                        bean.scope = "prototype"
                        bean.autowire = "byName"
                        bean.initMethod = "init"
                        map = lmb.map.clone()
                    }
                }
            }
        }

        zkgrailsComposerMapping(ComposerMapping.class) { bean ->
            bean.scope = "singleton"
            bean.autowire = "byName"
        }

        zkgrailsScaffoldingTemplate(org.zkoss.zk.grails.scaffolding.DefaultScaffoldingTemplate) { bean ->
            bean.scope = "prototype"
            bean.autowire = "byName"
        }
    }

    def doWithApplicationContext = { applicationContext ->
    }

    def doWithWebDescriptor = { xml ->
        //
        // e.g. ["zul"]
        //
        def supportExts = ZkConfigHelper.supportExtensions

        final String GOSIV_CLASS = "org.zkoss.zk.grails.web.ZKGrailsOpenSessionInViewFilter"

        //
        // e.g. ["*.zul", "/zkau/*"]
        //
        def filterUrls = supportExts.collect{ "*." + it } + ["/zkau/*"]

        // adding GrailsOpenSessionInView
        def filterElements = xml.'filter'[0]
        filterElements + {
            'filter' {
                'filter-name' ("GOSIVFilter")
                'filter-class' (GOSIV_CLASS)
            }
        }
        // filter for each ZK urls
        def filterMappingElements = xml.'filter-mapping'[0]
        filterUrls.each {p ->
            filterMappingElements + {
                'filter-mapping' {
                    'filter-name'("GOSIVFilter")
                    'url-pattern'("${p}")
                }
            }
        }

        //
        // e.g. ["*.zul", "*.dsp", "*.zhtml", "*.svg", "*.xml2html"]
        //
        def urls = supportExts.collect { "*." + it } + ["*.dsp", "*.zhtml", "*.svg", "*.xml2html"]

        // quick hack for page filtering
        def pageFilter = xml.filter.find { it.'filter-name'.text() == 'sitemesh' }
        def urlMappingFilter = xml.filter.find { it.'filter-name'.text() == 'urlMapping' }

        def grailsVersion = GrailsUtil.grailsVersion

        // Grails 1.3.x & Grails 2.0.x
        def pageFilterClass = "org.zkoss.zk.grails.web.ZKGrailsPageFilter"
        def urlMappingFilterClass = "org.zkoss.zk.grails.web.ZULUrlMappingsFilter"

        if(grailsVersion.startsWith("2")) {
            pageFilter.'filter-class'.replaceNode {
                'filter-class'(pageFilterClass)
            }
            urlMappingFilter.'filter-class'.replaceNode {
                'filter-class'(urlMappingFilterClass)
            }

            //
            // Require a legacy config for servlet version
            //
            if(application.metadata.getServletVersion() >= '3.0') {
                pageFilter.'filter-class' + {
                    'async-supported'('true')
                }
                urlMappingFilter.'filter-class' + {
                    'async-supported'('true')
                }
            }
        } else {
            pageFilter.'filter-class'.replaceBody(pageFilterClass)
            urlMappingFilter.'filter-class'.replaceBody(urlMappingFilterClass)
        }

        def listenerElements = xml.'listener'[0]
        listenerElements + {
            'listener' {
                'display-name' ("ZK Session Cleaner")
                'listener-class' ("org.zkoss.zk.ui.http.HttpSessionListener")
            }
        }

        def servletElements = xml.'servlet'[0]
        def mappingElements = xml.'servlet-mapping'[0]

        servletElements + {
            'servlet' {
                'servlet-name' ("zkLoader")
                'servlet-class' ("org.zkoss.zk.ui.http.DHtmlLayoutServlet")
                'init-param' {
                    'param-name' ("update-uri")
                    'param-value' ("/zkau")
                }
                'init-param' {
                    'param-name' ("compress")
                    'param-value' ("false")
                }
                'load-on-startup' (0)
            }
        }

        urls.each { p ->
            mappingElements + {
                'servlet-mapping' {
                    'servlet-name'("zkLoader")
                    'url-pattern'("${p}")
                }
            }
        }

        servletElements + {
            'servlet' {
                'servlet-name' ("auEngine")
                'servlet-class' ("org.zkoss.zk.au.http.DHtmlUpdateServlet")
            }
        }
        mappingElements + {
            'servlet-mapping' {
                'servlet-name'("auEngine")
                'url-pattern'("/zkau/*")
            }
        }

    }

    def doWithDynamicMethods = { ctx ->

        application.composerClasses.each { composerClass ->
            Class clazz = composerClass.clazz
            if(clazz.superclass == Script.class) {
                clazz.metaClass.methodMissing = { String name, args ->
                    if(name=='$') {
                        return JQuery.select(composer.root, args)
                    }
                    throw new MissingMethodException(name, delegate.class, args)
                }
            }
        }

        // Simpler way to add and remove event
        org.zkoss.zk.ui.AbstractComponent.metaClass.propertyMissing = { String name, handler ->
            if(name.startsWith("on") && handler instanceof Closure) {
                delegate.addEventListener(name, handler as EventListener)
            } else if(handler instanceof Closure) {
                delegate.setAttribute('$JQ_METHOD$_' + name, handler, Component.COMPONENT_SCOPE)
            } else  {
                throw new MissingPropertyException(name, delegate.class)
            }
        }

        // Simpler way to add and remove event
        org.zkoss.zk.ui.AbstractComponent.metaClass.methodMissing = { String name, args ->
            // converts OnXxxx to onXxxx
            name.metaClass.toEventName {return substring(indexOf("On"), length()).replace("On", "on")}

            if(name.startsWith("on") && args[0] instanceof Closure) {
                // register the new method to avoid methodMissing overhead
                org.zkoss.zk.ui.AbstractComponent.metaClass."${name}" { Closure listener ->
                    delegate.addEventListener(name, listener as EventListener)
                }
                delegate.addEventListener(name, args[0] as EventListener)
                return
            } else if (name.startsWith("addOn") && (args[0] instanceof Closure || args[0] instanceof EventListener)) {
                def eventName = name.toEventName()
                def listener = args[0] instanceof Closure ? args[0] as EventListener : args[0]
                org.zkoss.zk.ui.AbstractComponent.metaClass."${name}" { Closure handler ->
                    delegate.addEventListener(eventName, handler as EventListener)
                    handler as EventListener
                }
                // an overload version o add an EventListener directly
                org.zkoss.zk.ui.AbstractComponent.metaClass."${name}" << { EventListener evtListener ->
                    delegate.addEventListener(eventName, evtListener)
                    evtListener
                }
                delegate.addEventListener(eventName, listener)
                return listener
            } else if (name.startsWith("removeOn") && args[0] instanceof EventListener) {
                def eventName = name.toEventName()
                org.zkoss.zk.ui.AbstractComponent.metaClass."${name}" { EventListener listener ->
                    delegate.removeEventListener(eventName, listener)
                }
                return delegate.removeEventListener(eventName, args[0])
            } else {
                def meth = delegate.getAttribute('$JQ_METHOD$_' + name, Component.COMPONENT_SCOPE)
                if(meth) {
                    meth.delegate = delegate
                    return meth(args)
                }
                throw new MissingMethodException(name, delegate.class, args)
            }
        }

        org.zkoss.zk.ui.AbstractComponent.metaClass.append = { closure ->
            if(closure.delegate instanceof groovy.lang.Binding) {
                closure.delegate = new ZkBuilder(bind: closure.delegate, parent: delegate)
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
            } else {
                closure.delegate = new ZkBuilder(parent: delegate)
                closure.resolveStrategy = Closure.OWNER_FIRST
                closure.call()
            }
        }

        org.zkoss.zul.AbstractListModel.metaClass.getAt = { Integer i ->
            return delegate.getElementAt(i)
        }

        //
        // simple session
        //
        org.zkoss.zk.ui.http.SimpleSession.metaClass.getAt = { String name ->
            if(name == "id") {
                return Executions.current.getNativeRequest().getSession().getId()
            }
            delegate.getAttribute(name)
        }
        org.zkoss.zk.ui.http.SimpleSession.metaClass.putAt = { String name, value ->
            if(name == "id") return
            delegate.setAttribute(name, value)
        }
        org.zkoss.zk.ui.http.SimpleSession.metaClass.propertyMissing = { String name ->
            if(name == "id") {
                return Executions.current.getNativeRequest().getSession().getId()
            }
            delegate.getAttribute(name)
        }
        org.zkoss.zk.ui.http.SimpleSession.metaClass.propertyMissing << { String name, value ->
            if(name == "id") return
            delegate.setAttribute(name, value)
        }

        // Load specific components. Issue #103
        org.zkoss.zkplus.databind.DataBinder.metaClass.loadComponents = { List comps ->
            comps.each {
                delegate.loadComponent(it)
            }
        }

        // Support <listbox/> multiple = true. Issue #332
        org.zkoss.zul.Listbox.metaClass.setModel = {org.zkoss.zul.ListModel model ->
            ListboxExtender.setModel(delegate, model)
        }

        // Help getting model. Issue #336
        org.zkoss.zul.Listbox.metaClass.getModel = { ->
            ListboxExtender.getModel(delegate)
        }

    }

    def onChange = { event ->

        def context = event.ctx
        if (!context) {
            if (log.isDebugEnabled())
                log.debug("Application context not found. Can't reload")
            return
        }

        if(event.source instanceof FileSystemResource) {
            DevHolder devHolder = context.getBean('devHolder')
            synchronized(devHolder) {
                def fsr = (event.source as FileSystemResource)
                def pathToKeep = fsr.path.split("grails-app")[1].replace('\\','/')
                if(fsr.path.endsWith('.zul')) {
                    devHolder.add(pathToKeep, fsr.file)
                }
            }
        }

        if(event.source instanceof Class == false) return

        //
        //  Composer
        //
        if (application.isArtefactOfType(ComposerArtefactHandler.TYPE, event.source)) {
            def composerClass = application.addArtefact(ComposerArtefactHandler.TYPE, event.source)
            def composerBeanName = composerClass.propertyName
            if(composerClass.packageName) {
                composerBeanName = "${composerClass.packageName}.${composerBeanName}"
            }
            def beanDefinitions = beans {
                def clazz = composerClass.clazz
                if(clazz.superclass == Script.class) {
                    "${composerBeanName}"(JQueryComposer.class) { bean ->
                        bean.scope = "prototype"
                        bean.autowire = "byName"
                        innerComposer = clazz
                    }
                } else {
                    "${composerBeanName}"(composerClass.clazz) { bean ->
                        bean.scope = this.getScope(composerClass.clazz, "prototype")
                        bean.autowire = "byName"
                    }
                }
            }
            beanDefinitions.registerBeans(context)

            //
            // TODO: do refreshing the ZUL file
            // devHolder.add(pathToKeep, fsr.file)
            //
        }

        //
        // ViewModel
        //
        else if(application.isArtefactOfType(ViewModelArtefactHandler.TYPE, event.source)) {
            def viewModelClass = application.addArtefact(ViewModelArtefactHandler.TYPE, event.source)
            def beanDefinitions = beans {
                "${viewModelClass.propertyName}"(viewModelClass.clazz) { bean ->
                    bean.scope = this.getScope(viewModelClass.clazz, "prototype")
                    bean.autowire = 'byName'
                }
            }
            beanDefinitions.registerBeans(context)

        } else if (application.isArtefactOfType(FacadeArtefactHandler.TYPE, event.source)) {
            def facadeClass = application.addArtefact(FacadeArtefactHandler.TYPE, event.source)
            def beanDefinitions = beans {
                "${facadeClass.propertyName}"(facadeClass.clazz) { bean ->
                    bean.scope = this.getScope(facadeClass.clazz, "session")
                    bean.autowire = 'byName'
                }
            }
            beanDefinitions.registerBeans(context)

        } else if (application.isArtefactOfType(CometArtefactHandler.TYPE, event.source)) {
            def cometClass = application.addArtefact(CometArtefactHandler.TYPE, event.source)
            def beanDefinitions = beans {
                "${cometClass.propertyName}"(cometClass.clazz) { bean ->
                    bean.scope = this.getScope(cometClass.clazz, "prototype")
                    bean.autowire = 'byName'
                }
            }
            beanDefinitions.registerBeans(context)

        } else if (application.isArtefactOfType(LiveModelArtefactHandler.TYPE, event.source)) {
            def modelClass = application.addArtefact(LiveModelArtefactHandler.TYPE, event.source)
            def cfg = GCU.getStaticPropertyValue(modelClass.clazz, "config")
            if(cfg) {
                def lmb = new LiveModelBuilder()
                cfg.delegate = lmb
                cfg.resolveStrategy = Closure.DELEGATE_ONLY
                cfg.call()
                if (lmb.map['model'] == 'page') {
                    def beanDefinitions = beans {
                        "${modelClass.propertyName}"(SortingPagingListModel.class) { bean ->
                            bean.scope = 'prototype'
                            bean.autowire = 'byName'
                            bean.initMethod = 'init'
                            map = lmb.map.clone()
                        }
                    }
                    beanDefinitions.registerBeans(context)
                }
            }
        }
    }

    def onConfigChange = { event ->
    }

}

