package org.zkoss.zk.grails.web

import grails.util.Environment
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.io.Resource

import javax.servlet.ServletContext
import java.util.concurrent.ConcurrentHashMap

class ComposerMapping implements ApplicationContextAware, InitializingBean {

    private static final Log LOG = LogFactory.getLog(ComposerMapping.class)

    public static final String BEAN_NAME = "zkgrailsComposerMapping"

    ApplicationContext applicationContext
    GrailsApplication grailsApplication
    ServletContext servletContext

    Map<String, String> map

    /**
     * Construct a {@link Map} that contains mapping from a composer name to a file URI.
     *
     */
    void refresh() {
        map = new ConcurrentHashMap<String, String>()
        Resource[] resources
        if(!Environment.isWarDeployed()) {
            resources = applicationContext.getResources("file:./grails-app/zul/**/*.zul")
        } else {
            // TODO not tested yet
            String path = "file:/" + servletContext.getRealPath("WEB-INF") + "/grails-app/zul/**/*.zul"
            resources = applicationContext.getResources(path)
        }

        for(Resource r: resources) {
            String url = r.getURL().toString()
            String zulFilePath = url.substring(url.lastIndexOf("grails-app/zul") + 14)
            String lines = r.getInputStream().getText()
            def matcher = lines =~ /apply=\"([\w\.]+)\"/
            if(matcher.find()) {
                matcher.each { group ->
                    def composerName = group[1]
                    LOG.info("Found ${composerName} : ${r.getURL()}")
                    map[composerName.toLowerCase()] = zulFilePath
                }
            }
        }
    }

    /**
     * Map the composerPath to the related ZUL file.
     *
     * @param composerPath
     * @return URI of the ZUL file
     *
     **/
    public String resolveZul(String composerPath) {
        //
        //
        //
        def key = grailsApplication.composerClasses.find { it.logicalPropertyName == composerPath }?.fullName
        if(key)
            return map[key.toLowerCase()]
        else
            return null
    }

    /**
     * After the bean is instantiated, the method {@link #refresh()} will be called.
     **/
    public void afterPropertiesSet() throws Exception {
        try {
            refresh()
        } catch(Throwable e) {
            // ignore
        }
    }

}
