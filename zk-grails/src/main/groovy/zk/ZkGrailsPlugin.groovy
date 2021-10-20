package zk

import grails.core.ArtefactHandler
import grails.plugins.Plugin
import grails.util.Environment
import grails.util.GrailsClassUtils as GCU
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.boot.web.servlet.filter.OrderedFilter
import org.springframework.core.Ordered
import org.springframework.core.io.FileSystemResource
import org.zkoss.lang.Library
import org.zkoss.zk.au.http.DHtmlUpdateServlet
import org.zkoss.zk.grails.DesktopCounter
import org.zkoss.zk.grails.ZkBuilder
import org.zkoss.zk.grails.ZkConfigHelper
import org.zkoss.zk.grails.artefacts.*
import org.zkoss.zk.grails.composer.GrailsBindComposer
import org.zkoss.zk.grails.composer.JQueryComposer
import org.zkoss.zk.grails.dev.DevHolder
import org.zkoss.zk.grails.extender.ListboxExtender
import org.zkoss.zk.grails.livemodels.LiveModelBuilder
import org.zkoss.zk.grails.livemodels.SortingPagingListModel
import org.zkoss.zk.grails.scaffolding.DefaultScaffoldingTemplate
import org.zkoss.zk.grails.scope.DesktopScope
import org.zkoss.zk.grails.scope.PageScope
import org.zkoss.zk.grails.select.JQuery
import org.zkoss.zk.grails.web.ComposerMapping
import org.zkoss.zk.grails.web.ZKGrailsOpenSessionInViewFilter
import org.zkoss.zk.grails.web.ZKGrailsPageFilter
import org.zkoss.zk.grails.web.ZULUrlMappingsFilter
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.http.DHtmlLayoutServlet
import org.zkoss.zk.ui.http.HttpSessionListener

import javax.servlet.DispatcherType

@Slf4j
class ZkGrailsPlugin extends Plugin {
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "4.0.11 > *"

    def profiles = ["web"]

    def loadAfter = ["core", "controllers"]

    List<ArtefactHandler> artefacts = [
        CometArtefactHandler,
        ComposerArtefactHandler,
        FacadeArtefactHandler,
        LiveModelArtefactHandler,
        ViewModelArtefactHandler,
    ]

    def watchedResources = [
        "file:./grails-app/composers/**/*Composer.groovy",
        "file:./plugins/*/grails-app/composers/**/*Composer.groovy",
        "file:./grails-app/comets/**/*Comet.groovy",
        "file:./plugins/*/grails-app/comets/**/*Comet.groovy",
        "file:./grails-app/facade/**/*Facade.groovy",
        "file:./plugins/*/grails-app/facade/**/*Facade.groovy",
        "file:./grails-app/livemodels/**/*LiveModel.groovy",
        "file:./plugins/*/grails-app/livemodels/**/*LiveModel.groovy",
        "file:./grails-app/viewmodels/**/*ViewModel.groovy",
        "file:./plugins/*/grails-app/viewmodels/**/*ViewModel.groovy",
        "file:./grails-app/zul/**/*.zul" // support watching ZUL files
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
    def documentation = "http://grails.org/plugin/zk"
    def description = """\
A ZKGrails fork which provides support for Grails 4.

ZKGrails originated from Flyisland's ZK Plugin,
ZKGrails adds and enhances the ZK's RIA capabilities
and seamlessly integrates them with Grails' infrastructures.
"""

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "LGPL"

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Maicon Mauricio", email: "maiconandsilva@gmail.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "https://github.com/maiconandsilva/zk-grails/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/maiconandsilva/zk-grails/" ]

    @CompileStatic
    protected static String getScope(Class<?> clazz, String defaultScope) {
        return GCU.getStaticPropertyValue(clazz, "scope") ?: defaultScope
    }

    @Override
    Closure doWithSpring() { { ->

        Library.setProperty("org.zkoss.web.servlet.http.URLEncoder", "org.zkoss.zk.grails.web.URLEncoder")

        if (Environment.developmentMode) {
            devHolder(DevHolder) { bean ->
                bean.scope = "singleton"
            }
        }

        //
        // Registering new scopes
        //
        desktopScope(DesktopScope)
        pageScope(PageScope)
        zkgrailsScopesConfigurer(CustomScopeConfigurer) {
            scopes = [ 'desktop': ref('desktopScope'), 'page': ref('pageScope') ]
        }

        // Registering desktopCounter
        desktopCounter(DesktopCounter) { bean ->
            bean.scope = "singleton"
            bean.autowire = "byName"
        }

        //
        // Registering 'GrailsBindComposer'
        //
        grailsBindComposer(GrailsBindComposer) { bean ->
            bean.scope = 'prototype'
            bean.autowire = 'byName'
        }

        zkgrailsComposerMapping(ComposerMapping) { bean ->
            bean.scope = "singleton"
            bean.autowire = "byName"
        }

        zkgrailsScaffoldingTemplate(DefaultScaffoldingTemplate) { bean ->
            bean.scope = "prototype"
            bean.autowire = "byName"
        }

        // web.xml
        // Filter
        //
        // e.g. ["zul"]
        //
        def supportExts = ZkConfigHelper.supportExtensions
        boolean supportsAsync = grailsApplication.metadata.getServletVersion() >= "3.0"

        //
        // e.g. ["*.zul", "/zkau/*"]
        //
        def filterUrls = supportExts.collect{ "*." + it } + ["/zkau/*"]
        def urls = supportExts.collect { "*.$it" } + ["*.dsp", "*.zhtml", "*.svg", "*.xml2html"]

        // Servlet
        auEngine(ServletRegistrationBean, new DHtmlUpdateServlet(), "/zkau/*")

        zkLoader(ServletRegistrationBean, new DHtmlLayoutServlet(), urls as String[]) {
            initParameters = [ "update-uri": "/zkau", "compress": "false" ]
            loadOnStartup = 0
        }

        GOSIVFilter(FilterRegistrationBean) {
            filter = bean(ZKGrailsOpenSessionInViewFilter)
            urlPatterns = filterUrls
        }

        pageFilter(FilterRegistrationBean) {
            name = "sitemesh"
            filter = bean(ZKGrailsPageFilter)
            urlPatterns = ["/*"]
            order = OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER + 50
            asyncSupported = supportsAsync
            dispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR)
        }

        urlMappingFilter(FilterRegistrationBean) {
            name = "urlMapping"
            filter = bean(ZULUrlMappingsFilter)
            urlPatterns = ["/*"]
            order = OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER + 60
            asyncSupported = supportsAsync
            dispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD)
        }

        // Listener
        ZkSessionCleaner(ServletListenerRegistrationBean) {
            listener = bean(HttpSessionListener)
            order = Ordered.HIGHEST_PRECEDENCE
        }

        //
        // Registering ViewModel Beans to support MVVM
        //
        grailsApplication.viewModelClasses.each { viewModelClass ->
            "${viewModelClass.propertyName}"(viewModelClass.clazz) { bean ->
                bean.scope = getScope(viewModelClass.clazz, "prototype")
                bean.autowire = "byName"
            }
        }

        //
        // Registering Composer Beans
        //
        grailsApplication.composerClasses.each { composerClass ->
            def composerBeanName = composerClass.propertyName
            if (composerClass.packageName) {
                composerBeanName = "${composerClass.packageName}.${composerBeanName}"
            }
            def clazz = composerClass.clazz
            if (clazz.superclass == Script) {
                "${composerBeanName}"(JQueryComposer) { bean ->
                    bean.scope = "prototype"
                    bean.autowire = "byName"
                    innerComposer = clazz
                }
            } else {
                "${composerBeanName}"(composerClass.clazz) { bean ->
                    bean.scope = getScope(composerClass.clazz, "prototype")
                    bean.autowire = "byName"
                }
            }
        }

        //
        // Registering Facade Beans
        //
        grailsApplication.facadeClasses.each { facadeClass ->
            "${facadeClass.propertyName}"(facadeClass.clazz) { bean ->
                bean.scope = getScope(facadeClass.clazz, "session")
                bean.autowire = "byName"
            }
        }

        //
        // Registering Comet classes
        //
        grailsApplication.cometClasses.each { cometClass ->
            "${cometClass.propertyName}"(cometClass.clazz) { bean ->
                bean.scope = getScope(cometClass.clazz, "prototype")
                bean.autowire = "byName"
            }
        }

        //
        // Registering UI-Model classes
        //
        grailsApplication.liveModelClasses.each { modelClass ->
            def cfg = GCU.getStaticPropertyValue(modelClass.clazz, "config")
            if (cfg) {
                def lmb = new LiveModelBuilder()
                cfg.delegate = lmb
                cfg.resolveStrategy = Closure.DELEGATE_ONLY
                cfg.call()
                if (lmb.map['model'] == 'page') {
                    "${modelClass.propertyName}"(SortingPagingListModel) { bean ->
                        bean.scope = "prototype"
                        bean.autowire = "byName"
                        bean.initMethod = "init"
                        map = lmb.map.clone()
                    }
                }
            }
        }
    }}

    @Override
    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    @Override
    void doWithDynamicMethods() {
        grailsApplication.composerClasses.each { composerClass ->
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

    @Override
    void onChange(Map<String, Object> event) {
        def application = grailsApplication
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

        if(!(event.source instanceof Class)) return

        //
        //  Composer
        //
        if (application.isArtefactOfType(ComposerArtefactHandler.TYPE, event.source)) {
            def composerClass = application.addArtefact(ComposerArtefactHandler.TYPE, event.source)
            def composerBeanName = composerClass.propertyName
            if(composerClass.packageName) {
                composerBeanName = "${composerClass.packageName}.${composerBeanName}"
            }

            beans {
                def clazz = composerClass.clazz
                if(clazz.superclass == Script.class) {
                    "${composerBeanName}"(JQueryComposer.class) { bean ->
                        bean.scope = "prototype"
                        bean.autowire = "byName"
                        innerComposer = clazz
                    }
                } else {
                    "${composerBeanName}"(composerClass.clazz) { bean ->
                        bean.scope = getScope(composerClass.clazz, "prototype")
                        bean.autowire = "byName"
                    }
                }
            }

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
            beans {
                "${viewModelClass.propertyName}"(viewModelClass.clazz) { bean ->
                    bean.scope = getScope(viewModelClass.clazz, "prototype")
                    bean.autowire = 'byName'
                }
            }
        } else if (application.isArtefactOfType(FacadeArtefactHandler.TYPE, event.source)) {
            def facadeClass = application.addArtefact(FacadeArtefactHandler.TYPE, event.source)
            beans {
                "${facadeClass.propertyName}"(facadeClass.clazz) { bean ->
                    bean.scope = getScope(facadeClass.clazz, "session")
                    bean.autowire = 'byName'
                }
            }
        } else if (application.isArtefactOfType(CometArtefactHandler.TYPE, event.source)) {
            def cometClass = application.addArtefact(CometArtefactHandler.TYPE, event.source)
            beans {
                "${cometClass.propertyName}"(cometClass.clazz) { bean ->
                    bean.scope = getScope(cometClass.clazz, "prototype")
                    bean.autowire = 'byName'
                }
            }
        } else if (application.isArtefactOfType(LiveModelArtefactHandler.TYPE, event.source)) {
            def modelClass = application.addArtefact(LiveModelArtefactHandler.TYPE, event.source)
            def cfg = GCU.getStaticPropertyValue(modelClass.clazz, "config")
            if(cfg) {
                def lmb = new LiveModelBuilder()
                cfg.delegate = lmb
                cfg.resolveStrategy = Closure.DELEGATE_ONLY
                cfg.call()
                if (lmb.map['model'] == 'page') {
                    beans {
                        "${modelClass.propertyName}"(SortingPagingListModel.class) { bean ->
                            bean.scope = 'prototype'
                            bean.autowire = 'byName'
                            bean.initMethod = 'init'
                            map = lmb.map.clone()
                        }
                    }
                }
            }
        }
    }

    @Override
    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    @Override
    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}

