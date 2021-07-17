/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zkoss.zk.grails.web;

import com.opensymphony.module.sitemesh.html.util.CharArray;
import com.opensymphony.module.sitemesh.parser.TokenizedHTMLPage;
import com.opensymphony.sitemesh.compatability.HTMLPage2Content;
import grails.util.Environment;
import grails.util.GrailsWebUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.ApplicationAttributes;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.support.NullPersistentContextInterceptor;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;
import org.codehaus.groovy.grails.web.sitemesh.*;
import org.codehaus.groovy.grails.web.util.StreamCharBuffer;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.util.UrlPathHelper;
import org.zkoss.zk.grails.ZkConfigHelper;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.sitemesh.Content;
import com.opensymphony.sitemesh.ContentProcessor;
import com.opensymphony.sitemesh.Decorator;
import com.opensymphony.sitemesh.compatability.OldDecorator2NewDecorator;
import com.opensymphony.sitemesh.compatability.PageParser2ContentProcessor;
import com.opensymphony.sitemesh.webapp.ContainerTweaks;
import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import com.opensymphony.sitemesh.webapp.SiteMeshWebAppContext;

/**
 * Extends the default page filter to overide the apply decorator behaviour
 * if the page is a GSP
 *
 * @author Graeme Rocher
 */
public class ZKGrailsPageFilter extends SiteMeshFilter {

    public static final String ALREADY_APPLIED_KEY = "com.opensymphony.sitemesh.APPLIED_ONCE";
    public static final String FACTORY_SERVLET_CONTEXT_ATTRIBUTE = "sitemesh.factory";
    private static final String HTML_EXT = ".html";
    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String CONFIG_OPTION_GSP_ENCODING = "grails.views.gsp.encoding";
    public static final String GSP_SITEMESH_PAGE = GrailsPageFilter.class.getName() + ".GSP_SITEMESH_PAGE";

    private FilterConfig filterConfig;
    private ContainerTweaks containerTweaks;
    private WebApplicationContext applicationContext;
    private PersistenceContextInterceptor persistenceInterceptor = new NullPersistentContextInterceptor();
    private String defaultEncoding = UTF_8_ENCODING;
    protected ViewResolver layoutViewResolver;
    private ContentProcessor contentProcessor;
    private DecoratorMapper decoratorMapper;

    @Override
    public void init(FilterConfig fc) {
        super.init(fc);
        this.filterConfig = fc;
        containerTweaks = new ContainerTweaks();
        Config config = new Config(fc);

        Grails5535Factory defaultFactory = new Grails5535Factory(config); // TODO revert once Sitemesh bug is fixed
        fc.getServletContext().setAttribute(FACTORY_SERVLET_CONTEXT_ATTRIBUTE, defaultFactory);
        defaultFactory.refresh();
        FactoryHolder.setFactory(defaultFactory);

        contentProcessor = new PageParser2ContentProcessor(defaultFactory);
        decoratorMapper = defaultFactory.getDecoratorMapper();

        applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(fc.getServletContext());
        layoutViewResolver = WebUtils.lookupViewResolver(applicationContext);

        final GrailsApplication grailsApplication = GrailsWebUtil.lookupApplication(fc.getServletContext());
        String encoding = (String) grailsApplication.getFlatConfig().get(CONFIG_OPTION_GSP_ENCODING);
        if (encoding != null) {
            defaultEncoding = encoding;
        }

        Map<String, PersistenceContextInterceptor> interceptors = applicationContext.getBeansOfType(PersistenceContextInterceptor.class);
        if (!interceptors.isEmpty()) {
            persistenceInterceptor = interceptors.values().iterator().next();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        FactoryHolder.setFactory(null);
    }

    private String extractRequestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String query = request.getQueryString();
        return (servletPath == null ? "" : servletPath)
                + (pathInfo == null ? "" : pathInfo)
                + (query == null ? "" : ("?" + query));
    }

    private boolean isZUL(HttpServletRequest request) {
        String path = extractRequestPath(request);
        if(path.indexOf("?")!=-1) {
            path = path.split("\\?")[0];
        } else if (path.indexOf("#")!=-1){
            path = path.split("#")[0];
        }
        ArrayList<String> arrExtensions = ZkConfigHelper.getSupportExtensions();
        for(String sExt : arrExtensions) {
            if(path.lastIndexOf("." + sExt) != -1) return true;
        }
        return false;
    }

    private boolean isZK(HttpServletRequest request) {
        String path = extractRequestPath(request);
        if(path.indexOf("/zkau") != -1) return true;
        if(path.indexOf("/zkcomet") != -1) return true;

        //
        // Extended checking in support extension configuration
        // By default, ["zul"] will be checked here
        //
        ArrayList<String> arrExtensions = ZkConfigHelper.getSupportExtensions();
        for(String sExt : arrExtensions) {
            if(path.lastIndexOf("." + sExt) != -1) return true;
        }

        final String[] ext = new String[]{".dsp",".zhtml", ".svg", ".xml2html"};
        for(int i=0;i < ext.length; i++) {
            if(path.lastIndexOf(ext[i]) != -1) return true;
        }

        return false;
    }

    /*
     * TODO: This method has been copied from the parent to fix a bug in sitemesh 2.3. When sitemesh 2.4 is release this method and the two private methods below can removed
     *
     * Main method of the Filter.
     *
     * <p>Checks if the Filter has been applied this request. If not, parses the page
     * and applies {@link com.opensymphony.module.sitemesh.Decorator} (if found).
     */
    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain)
                throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) rq;
        HttpServletResponse response = (HttpServletResponse) rs;
        ServletContext servletContext = filterConfig.getServletContext();

        SiteMeshWebAppContext webAppContext = new SiteMeshWebAppContext(request, response, servletContext);

        if (filterAlreadyAppliedForRequest(request)) {
            // Prior to Servlet 2.4 spec, it was unspecified whether the filter should be called again upon an include().
            chain.doFilter(request, response);
            return;
        }

        if(isZUL(request)) {
            //
            // TODO if disable live
            //
            Content content = obtainContent(contentProcessor, webAppContext, request, response, chain);
            if (content == null || response.isCommitted()) {
                return;
            }
            Content content2 = applyLive(request, content);
            new GrailsNoDecorator().render(content2, webAppContext);
            return;
        }

        if(isZK(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (!contentProcessor.handles(webAppContext)) {
            // Optimization: If the content doesn't need to be processed, bypass SiteMesh.
            chain.doFilter(request, response);
            return;
        }

        // clear the page in case it is already present
        request.removeAttribute(RequestConstants.PAGE);

        if (containerTweaks.shouldAutoCreateSession()) {
            request.getSession(true);
        }

        boolean dispatched = false;
        try {
            persistenceInterceptor.init();
            Content content = obtainContent(contentProcessor, webAppContext, request, response, chain);
            if (content == null || response.isCommitted()) {
                return;
            }
            // applyLive(request, content);
            detectContentTypeFromPage(content, response);
            com.opensymphony.module.sitemesh.Decorator decorator = decoratorMapper.getDecorator(request, GSPSitemeshPage.content2htmlPage(content));
            if(decorator instanceof Decorator) {
                ((Decorator)decorator).render(content, webAppContext);
            } else {
                new OldDecorator2NewDecorator(decorator).render(content, webAppContext);
            }
            dispatched = true;
        }
        catch (IllegalStateException e) {
            // Some containers (such as WebLogic) throw an IllegalStateException when an error page is served.
            // It may be ok to ignore this. However, for safety it is propegated if possible.
            if (!containerTweaks.shouldIgnoreIllegalStateExceptionOnErrorPage()) {
                throw e;
            }
        }
        finally {
            if (!dispatched) {
                // an error occured
                request.setAttribute(ALREADY_APPLIED_KEY, null);
            }
            if (persistenceInterceptor.isOpen()) {
                persistenceInterceptor.flush();
                persistenceInterceptor.destroy();
            }
        }
    }

    private Content applyLive(HttpServletRequest request, Content content) throws IOException {
        if(Environment.getCurrent() == Environment.DEVELOPMENT) {
            // if ZK Grails in the Dev mode
            // insert z-it-live.js
            if(content instanceof GSPSitemeshPage) {
                GSPSitemeshPage page = (GSPSitemeshPage)content;
                String pageContent = page.getPage();
                if(pageContent == null) {
                    return content;
                }
                String contextPath = request.getContextPath();
                //
                // src="/zello/zkau/
                if(pageContent.indexOf("src=\""+ contextPath + "/zkau/") > 0) {
                    StreamCharBuffer buffer = new StreamCharBuffer();
                    LinkGenerator grailsLinkGenerator = (LinkGenerator) applicationContext.getBean("grailsLinkGenerator");
                    String link = grailsLinkGenerator.resource(new HashMap(){{
                        put("dir","ext/js");
                        put("file","z-it-live.js");
                        put("plugin", "zk");
                    }});
                    link = link.replaceAll("/plugins", "/static/plugins");
                    buffer.getWriter().write(
                        pageContent.replace("</head>",
                            "<script type=\"text/javascript\" src=\""  + link + "\" charset=\"UTF-8\"></script>\n</head>")
                    );
                    page.setPageBuffer(buffer);
                }
                return content;
            } else if(content instanceof HTMLPage2Content) {
                HTMLPage2Content page2Content = (HTMLPage2Content)content;
                try {
                    Field fPage = HTMLPage2Content.class.getDeclaredField("page");
                    fPage.setAccessible(true);
                    GrailsTokenizedHTMLPage htmlPage = (GrailsTokenizedHTMLPage)fPage.get(page2Content);
                    String pageContent = htmlPage.getPage();
                    String head = htmlPage.getHead();
                    String body = htmlPage.getBody();

                    String contextPath = request.getContextPath();
                    //
                    // src="/zello/zkau/
                    if(pageContent.indexOf("src=\""+ contextPath + "/zkau/") > 0) {
                        LinkGenerator grailsLinkGenerator = (LinkGenerator) applicationContext.getBean("grailsLinkGenerator");
                        String link = grailsLinkGenerator.resource(new HashMap(){{
                            put("dir","ext/js");
                            put("file","z-it-live.js");
                            put("plugin", "zk");
                        }});
                        link = link.replaceAll("/plugins", "/static/plugins");
                        pageContent = pageContent.replace("</head>", "<script type=\"text/javascript\" src=\"" + link + "\" charset=\"UTF-8\"></script>\n</head>");
                        head = head + "\n<script type=\"text/javascript\" src=\""  + link + "\" charset=\"UTF-8\"></script>\n";
                        CharArray newBody = new CharArray(body.length());
                        newBody.append(body);
                        CharArray newHead = new CharArray(head.length());
                        newHead.append(head);
                        GrailsTokenizedHTMLPage newHtmlPage = new GrailsTokenizedHTMLPage(pageContent.toCharArray(), newBody, newHead);
                        return new HTMLPage2Content(newHtmlPage);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    private String detectZULFile(String pageContent, String contextPath) {
        // detect uu:'/zello/zkau'
        int i = pageContent.indexOf("uu:'"+ contextPath + "/zkau'");
        if(i < 0)
            return null;
        i += 4;
        int j = i;
        while(pageContent.charAt(j) != '\'') {
            j++;
            if(j >= pageContent.length()) return null;
        }
        return pageContent.substring(i, j);
    }

    /**
     * Continue in filter-chain, writing all content to buffer and parsing
     * into returned {@link com.opensymphony.module.sitemesh.Page} object. If
     * {@link com.opensymphony.module.sitemesh.Page} is not parseable, null is returned.
     */
    private Content obtainContent(ContentProcessor contentProcessor, SiteMeshWebAppContext webAppContext,
                                  HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException {

        Object oldGspSiteMeshPage=request.getAttribute(GSP_SITEMESH_PAGE);
        try {
            request.setAttribute(GSP_SITEMESH_PAGE, new GSPSitemeshPage());
            GrailsContentBufferingResponse contentBufferingResponse = new GrailsContentBufferingResponse(
                    response, contentProcessor, webAppContext);

            setDefaultConfiguredEncoding(request, contentBufferingResponse);
            chain.doFilter(request, contentBufferingResponse);
            // TODO: check if another servlet or filter put a page object in the request
            //            Content result = request.getAttribute(PAGE);
            //            if (result == null) {
            //                // parse the page
            //                result = pageResponse.getPage();
            //            }
            webAppContext.setUsingStream(contentBufferingResponse.isUsingStream());
            return contentBufferingResponse.getContent();
        }
        finally {
            if (oldGspSiteMeshPage != null) {
                request.setAttribute(GSP_SITEMESH_PAGE, oldGspSiteMeshPage);
            }
        }
    }

    private void setDefaultConfiguredEncoding(HttpServletRequest request, GrailsContentBufferingResponse contentBufferingResponse) {
        UrlPathHelper urlHelper = new UrlPathHelper();
        String requestURI = urlHelper.getOriginatingRequestUri(request);
        // static content?
        if (requestURI.endsWith(HTML_EXT)) {
            contentBufferingResponse.setContentType("text/html;charset="+defaultEncoding);
        }
    }

    private boolean filterAlreadyAppliedForRequest(HttpServletRequest request) {
        if (request.getAttribute(ALREADY_APPLIED_KEY) == Boolean.TRUE) {
            return true;
        }

        request.setAttribute(ALREADY_APPLIED_KEY, Boolean.TRUE);
        return false;
    }

    private void detectContentTypeFromPage(Content page, HttpServletResponse response) {
        String contentType = page.getProperty("meta.http-equiv.Content-Type");
        if (contentType != null && "text/html".equals(response.getContentType())) {
            response.setContentType(contentType);
        }
    }
}
