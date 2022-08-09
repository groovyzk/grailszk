package org.zkoss.web.util.resource;

import grails.core.GrailsApplication;
import groovy.lang.Writable;
import groovy.text.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.grails.core.io.DefaultResourceLocator;
import org.grails.gsp.GroovyPagesTemplateEngine;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.util.resource.Locator;
import org.zkoss.web.servlet.Servlets;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.metainfo.PageDefinitions;
import org.zkoss.zk.ui.metainfo.Parser;

import java.io.*;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("deprecation")
public class GrailsContentLoader extends ResourceLoader<PageDefinition> {
    private static final String GROOVY_PAGES_TEMPLATE_ENGINE = "groovyPagesTemplateEngine";
    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String CONFIG_OPTION_GSP_ENCODING = "grails.views.gsp.encoding";
    private static final String CONFIG_ZKGRAILS_TAGLIB_DISABLE = "grails.zk.taglib.disabled";
    private static final Log log = LogFactory.getLog(GrailsContentLoader.class);
    private final WebApp webApp;
    private final ApplicationContext appCtx;
    private final GrailsApplication grailsApplication;
    private final ResourceUtils resourceUtils;
    
    public GrailsContentLoader(final WebApp webApp) {
        this.webApp = webApp;
        final WebApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(webApp.getServletContext());
        grailsApplication = ctx.getBean("grailsApplication", GrailsApplication.class);
        appCtx = grailsApplication.getMainContext();
        appCtx.getBean("grailsResourceLocator", DefaultResourceLocator.class);
        resourceUtils = new ResourceUtils(appCtx);
    }

    @Override
    public PageDefinition load(final ResourceInfo si) throws Exception {
        final org.springframework.core.io.Resource springResource = resourceUtils.getResource(si.path);

        if (springResource.exists()) {
            log.debug("Load from Spring Resource: " + springResource);
            try {
                return parse(si.path, springResource, si.extra);
            } catch (Throwable e) {
                log.debug("Cannot parse ZUL from a Spring Resource", e);
                throw (Exception) e;
            }
        }

        if (si.url != null) {
            log.debug("Load from URL: " + si.url);
            return parse(si.path, si.url, si.extra);
        }

        if (!si.file.exists()) {
            log.debug("File " + si.file + " not found");
            return null;
        }

        try {
            log.debug("Load from File: " + si.file);
            return parse(si.path, si.file, si.extra);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    
    private StringReader preprocessGSP(final Map<?, ?> config, final long length, final InputStream in)
            throws IOException {
        log.debug("Enter :: preprocessGSP");
        final GroovyPagesTemplateEngine gsp = (GroovyPagesTemplateEngine) appCtx.getBean(GROOVY_PAGES_TEMPLATE_ENGINE);
        log.debug("Got GSP Template bean: " + gsp);
        byte[] buffer;
        final UnicodeBOMInputStream ubomIn = new UnicodeBOMInputStream(in);
        if (ubomIn.getBOM() != UnicodeBOMInputStream.BOM.NONE) {
            log.debug("BOM detected");
            ubomIn.skipBOM();
            buffer = new byte[(int)length - ubomIn.getBOM().getBytes().length];
        }
        else {
            buffer = new byte[(int)length];
        }
        final BufferedInputStream bis = new BufferedInputStream(ubomIn);
        bis.read(buffer);
        String encoding = (String) config.get(CONFIG_OPTION_GSP_ENCODING);
        if (encoding == null) {
            encoding = UTF_8_ENCODING;
        }
        String bufferStr = new String(buffer, encoding).replaceAll("@\\{", "\\$\\{'@'\\}\\{");
        bufferStr = TagDehyphen.dehyphen(bufferStr);

        final Template template = gsp.createTemplate(new ByteArrayResource(bufferStr.getBytes(encoding)), false);
        final Writable w = template.make();
        final StringWriter sw = new StringWriter();
        w.writeTo(new PrintWriter(sw));
        final String zulSrc = sw.toString().replaceAll("\\#\\{", "\\$\\{");
        final StringReader reader = new StringReader(zulSrc);
        log.debug("Returning pre-processed ::: " + reader);
        return reader;
    }

    private PageDefinition parse(final String path, final org.springframework.core.io.Resource resource,
                                 final Object extra) throws Throwable {
        final Map<?, ?> config = grailsApplication.getConfig().flatten();
        final Boolean disable = (Boolean)config.get(CONFIG_ZKGRAILS_TAGLIB_DISABLE);
        final Locator locator = (Locator)((extra != null) ? extra : PageDefinitions.getLocator(webApp, path));
        if (disable != null && disable) {
            return new Parser(webApp, locator).parse(new InputStreamReader(resource.getInputStream()), path);
        }
        final StringReader reader = preprocessGSP(config, resource.contentLength(), resource.getInputStream());
        final PageDefinition pgdef = new Parser(webApp, locator).parse(reader, Servlets.getExtension(path));
        pgdef.setRequestPath(path);
        return pgdef;
    }

    @Override
    protected PageDefinition parse(final String path, final URL url, final Object extra) throws Exception {
        final Locator locator = (Locator)((extra != null) ? extra : PageDefinitions.getLocator(webApp, path));
        return new Parser(webApp, locator).parse(url, path);
    }

    @Override
    protected PageDefinition parse(final String path, final File file, final Object extra) throws Exception {
        final GrailsApplication grailsApplication = (GrailsApplication) appCtx.getBean("grailsApplication");
        final Map<?, ?> config = grailsApplication.getConfig().flatten();
        final Boolean disable = (Boolean) config.get(CONFIG_ZKGRAILS_TAGLIB_DISABLE);
        final Locator locator = (Locator)((extra != null) ? extra : PageDefinitions.getLocator(webApp, path));
        if (disable != null && disable) {
            return new Parser(webApp, locator).parse(file, path);
        }
        final StringReader reader = preprocessGSP(config, file.length(), new FileInputStream(file));
        final PageDefinition pgdef = new Parser(webApp, locator).parse(reader, Servlets.getExtension(path));
        pgdef.setRequestPath(path);
        return pgdef;
    }
}
