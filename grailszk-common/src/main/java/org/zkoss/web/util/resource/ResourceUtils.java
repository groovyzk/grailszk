package org.zkoss.web.util.resource;

import grails.util.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author Maicon Mauricio
 */
public class ResourceUtils {
    private final ApplicationContext applicationContext;

    public ResourceUtils(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public org.springframework.core.io.Resource getResource(String path) {
        org.springframework.core.io.Resource springResource =
                applicationContext.getResource("WEB-INF/classes" + path);

        if (Environment.isDevelopmentEnvironmentAvailable() && !springResource.exists()) {
            springResource = applicationContext.getResource("file:./grails-app/zul" + path);
        }

        if (!springResource.exists()) { // .zul file might be in a subproject or plugin
            springResource = new ClassPathResource(path);
        }
        return springResource;
    }

    public String getRealPath(String path) {
        org.springframework.core.io.Resource springResource = getResource(path);
        try {
            if (springResource.exists()) {
                return springResource.getURI().toString();
            }
        } catch (IOException ignored) {}
        return null;
    }
}
