package org.zkoss.zk.grails.ui;

import grails.core.GrailsApplication;
import org.springframework.context.ApplicationContext;
import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.http.SimpleUiFactory;

public class GrailsComposerFactory extends SimpleUiFactory {
    public Composer<?> newComposer(final Page page, final Class klass) {
        Versions.versionValidator();
        return (Composer<?>) super.newComposer(page, klass);
    }
    
    public Composer<?> newComposer(final Page page, String className) throws ClassNotFoundException {
        Versions.versionValidator();
        final ServletContext servletContext = page.getDesktop().getWebApp().getServletContext();
        final ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        final GrailsApplication grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication");
        final ApplicationContext mainContext = grailsApplication.getMainContext();
        final String[] result = className.split("\\.");
        final String classNamePart = result[result.length - 1];

        if (Character.isUpperCase(classNamePart.charAt(0))) {
            result[result.length - 1] = StringUtils.uncapitalize(classNamePart);
            className = StringUtils.join(result, ".");
        }

        if (mainContext.containsBean(className)) {
            return (Composer<?>) mainContext.getBean(className);
        }
        return (Composer<?>) super.newComposer(page, className);
    }
}
