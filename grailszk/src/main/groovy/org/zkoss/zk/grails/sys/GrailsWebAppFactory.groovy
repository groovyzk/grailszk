package org.zkoss.zk.grails.sys

import grails.util.Environment
import org.zkoss.lang.Library
import org.zkoss.zk.grails.test.TestIdGenerator
import org.zkoss.zk.grails.ui.GrailsComposerFactory
import org.zkoss.zk.ui.UiException
import org.zkoss.zk.ui.WebApp
import org.zkoss.zk.ui.http.SimpleWebApp
import org.zkoss.zk.ui.util.Configuration
import org.zkoss.zkplus.util.ThreadLocalListener

class GrailsWebAppFactory implements org.zkoss.zk.ui.sys.WebAppFactory {

    @Override
    WebApp newWebApp(java.lang.Object ctx, Configuration config)  {
        if(Environment.current == Environment.TEST) {
            config.setIdGeneratorClass(TestIdGenerator)
        }
        config.setUiFactoryClass(GrailsComposerFactory)
        config.addListener(ThreadLocalListener)
        Library.setProperty("org.zkoss.zk.ui.metainfo.page.Loader.class", "org.zkoss.web.util.resource.GrailsContentLoader")

        WebApp wapp
        def cls = config.getWebAppClass()
        if (cls != null) {
            try {
                wapp = (WebApp)cls.newInstance()
            } catch (Exception ex) {
                throw UiException.Aide.wrap(ex, "Unable to construct "+cls)
            }
        } else {
             wapp = new SimpleWebApp()
        }
        return wapp
    }

}