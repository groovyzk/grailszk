package org.zkoss.zk.grails.web;

/* Copyright 2004-2005 Graeme Rocher
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

import grails.config.Config;
import grails.core.GrailsApplication;
import grails.core.GrailsClass;
import grails.util.Environment;
import grails.util.GrailsClassUtils;
import grails.util.Holders;
import grails.util.Metadata;
import grails.web.UrlConverter;
import grails.web.mapping.UrlMapping;
import grails.web.mapping.UrlMappingInfo;
import grails.web.mapping.UrlMappingsHolder;
import grails.web.mapping.exceptions.UrlMappingException;
import grails.web.mime.MimeType;
import groovy.util.ConfigObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.grails.core.artefact.ControllerArtefactHandler;
import org.grails.exceptions.reporting.DefaultStackTraceFilterer;
import org.grails.exceptions.reporting.StackTraceFilterer;
import org.grails.gsp.GroovyPagesException;
import org.grails.web.errors.GrailsExceptionResolver;
import org.grails.web.mapping.RegexUrlMapping;
import org.grails.web.mapping.UrlMappingUtils;
import org.grails.web.servlet.WrappedResponseHolder;
import org.grails.web.servlet.mvc.GrailsWebRequest;
import org.grails.web.util.GrailsApplicationAttributes;
import org.grails.web.util.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.util.UrlPathHelper;
import org.zkoss.zk.grails.artefacts.ComposerArtefactHandler;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses the Grails UrlMappings to match and forward requests to a relevant controller and action.
 * Support for ZK's .zul files.
 *
 * @author Graeme Rocher
 * @author Chanwit Kaewkasi
 * @author Maicon Mauricio
 * @since  2.0.0.M2
 */
public class ZULUrlMappingsFilter extends OncePerRequestFilter {

    public static final boolean WAR_DEPLOYED = Metadata.getCurrent().isWarDeployed();
    private final UrlPathHelper urlHelper = new UrlPathHelper();
    private static final Log LOG = LogFactory.getLog(ZULUrlMappingsFilter.class);
    private static final String GSP_SUFFIX = ".gsp";
    private static final String JSP_SUFFIX = ".jsp";
    private static final String ZUL_SUFFIX = ".zul";
    private HandlerInterceptor[] handlerInterceptors = new HandlerInterceptor[0];
    private GrailsApplication application;
    private Config grailsConfig; // TODO: verify
    private ViewResolver viewResolver;
    private MimeType[] mimeTypes;
    private StackTraceFilterer filterer;

    private UrlConverter urlConverter;
    private ComposerMapping composerMapping;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        urlHelper.setUrlDecode(false);
        final ServletContext servletContext = getServletContext();
        final WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        handlerInterceptors = WebUtils.lookupHandlerInterceptors(servletContext);
        application = WebUtils.lookupApplication(servletContext);
        viewResolver = WebUtils.lookupViewResolver(servletContext);
        ApplicationContext mainContext = application.getMainContext();
        urlConverter = mainContext.getBean(UrlConverter.BEAN_NAME, UrlConverter.class);
        if (application != null) {
            grailsConfig = Holders.getConfig(); // TODO: verify
        }

        if (applicationContext.containsBean(MimeType.BEAN_NAME)) {
            this.mimeTypes = applicationContext.getBean(MimeType.BEAN_NAME, MimeType[].class);
        }

        composerMapping = applicationContext.getBean(ComposerMapping.BEAN_NAME, ComposerMapping.class);

        createStackTraceFilterer();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        UrlMappingsHolder holder = UrlMappingUtils.lookupUrlMappings(getServletContext()); // TODO: verify

        String uri = urlHelper.getPathWithinApplication(request);

        if (uri.startsWith("/zkau")      ||
            uri.startsWith("/zkcomet")   ||
            uri.startsWith("/dbconsole") ||
            uri.startsWith("/ext")       ||
            uri.startsWith("~.")) {
            LOG.debug("Excluding: " + uri);
            processFilterChain(request, response, filterChain);
            return;
        }

        if (!"/".equals(uri) && noControllers() && noComposers() && noRegexMappings(holder)) {
            // not index request, no controllers, and no URL mappings for views, so it's not a Grails request
            LOG.debug("not index request, no controllers, and no URL mappings for views, so it's not a Grails request");
            processFilterChain(request, response, filterChain);
            return;
        }

        if (isUriExcluded(holder, uri)) {
            LOG.debug("Excluded by pattern: " + uri);
            processFilterChain(request, response, filterChain);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing URL mapping filter...");
            LOG.debug(holder);
        }

        if (areFileExtensionsEnabled()) {
            String format = WebUtils.getFormatFromURI(uri, mimeTypes);
            if (format != null) {
                MimeType[] configuredMimes = mimeTypes == null ? MimeType.getConfiguredMimeTypes() : mimeTypes;
                // only remove the file extension if it's one of the configured mimes in Config.groovy
                for (MimeType configuredMime : configuredMimes) {
                    if (configuredMime.getExtension().equals(format)) {
                        request.setAttribute(GrailsApplicationAttributes.RESPONSE_FORMAT, format);
                        uri = uri.substring(0, (uri.length() - format.length() - 1));
                        break;
                    }
                }
            }
        }

        GrailsWebRequest webRequest = (GrailsWebRequest)request.getAttribute(GrailsApplicationAttributes.WEB_REQUEST);
        UrlMappingInfo[] urlInfos = holder.matchAll(uri);
        WrappedResponseHolder.setWrappedResponse(response);
        boolean dispatched = false;
        try {
            // GRAILS-3369: Save the original request parameters.
            Map backupParameters;
            try {
                backupParameters = new HashMap(webRequest.getParams());
            } catch (Exception e) {
                LOG.error("Error creating params object: " + e.getMessage(), e);
                backupParameters = Collections.EMPTY_MAP;
            }

            for (UrlMappingInfo info : urlInfos) {
                if (info != null) {
                    // GRAILS-3369: The configure() will modify the
                    // parameter map attached to the web request. So,
                    // we need to clear it each time and restore the
                    // original request parameters.
                    webRequest.getParams().clear();
                    webRequest.getParams().putAll(backupParameters);

                    final String viewName;
                    try {
                        info.configure(webRequest);
                        String action = info.getActionName() == null ? "" : info.getActionName();
                        viewName = info.getViewName();
                        if (viewName == null && info.getURI() == null) {
                            final String controllerName = info.getControllerName();
                            String pluginName = info.getPluginName();
                            String featureUri = WebUtils.SLASH + urlConverter.toUrlElement(controllerName) + WebUtils.SLASH + urlConverter.toUrlElement(action);

                            Object featureId = null;
                            if (pluginName != null) {
                                Map featureIdMap = new HashMap();
                                featureIdMap.put("uri", featureUri);
                                featureIdMap.put("pluginName", pluginName);
                                featureId = featureIdMap;
                            } else {
                                featureId = featureUri;
                            }
                            GrailsClass controller = application.getArtefactForFeature(ControllerArtefactHandler.TYPE, featureId);
                            if (controller == null) {
                                if(uri.endsWith(".zul")) {
                                    RequestDispatcher dispatcher = request.getRequestDispatcher(uri);
                                    dispatcher.forward(request, response);
                                    dispatched = true;
                                    break;
                                }
                                String zul = composerMapping.resolveZul(controllerName);
                                if(zul != null) {
                                    RequestDispatcher dispatcher = request.getRequestDispatcher(zul);
                                    dispatcher.forward(request, response);
                                    dispatched = true;
                                    break;
                                }
                            } else {
                                webRequest.setAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE, controller.getLogicalPropertyName(), WebRequest.SCOPE_REQUEST);
                                webRequest.setAttribute(GrailsApplicationAttributes.GRAILS_CONTROLLER_CLASS, controller, WebRequest.SCOPE_REQUEST);
                                // webRequest.setAttribute(GrailsApplicationAttributes. GRAILS_CONTROLLER_CLASS_AVAILABLE, Boolean.TRUE, WebRequest.SCOPE_REQUEST);
                            }
                        }
                    } catch (Exception e) {
                        if (e instanceof MultipartException) {
                            reapplySitemesh(request);
                            throw ((MultipartException)e);
                        }
                        LOG.error("Error when matching URL mapping [" + info + "]:" + e.getMessage(), e);
                        continue;
                    }

                    dispatched = true;

                    if (!WAR_DEPLOYED) {
                        checkDevelopmentReloadingState(request);
                    }

                    request = checkMultipart(request);

                    if (viewName == null || (viewName.endsWith(GSP_SUFFIX) || viewName.endsWith(JSP_SUFFIX))) {
                        if (info.isParsingRequest()) {
                            webRequest.informParameterCreationListeners();
                        }
                        String forwardUrl = UrlMappingUtils.forwardRequestForUrlMappingInfo(request, response, info, Collections.emptyMap(), true);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Matched URI [" + uri + "] to URL mapping [" + info + "], forwarding to [" + forwardUrl + "] with response [" + response.getClass() + "]");
                        }
                    } else if(viewName.endsWith(ZUL_SUFFIX)) {
                        RequestDispatcher dispatcher = request.getRequestDispatcher(viewName);
                        dispatcher.forward(request, response);
                    } else {
                        if (!renderViewForUrlMappingInfo(request, response, info, viewName)) {
                            dispatched = false;
                        }
                    }
                    break;
                }
            } // for
        } finally {
            WrappedResponseHolder.setWrappedResponse(null);
        }

        if (!dispatched) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No match found, processing remaining filter chain.");
            }
            processFilterChain(request, response, filterChain);
        }
    }

    public static boolean isUriExcluded(UrlMappingsHolder holder, String uri) {
        boolean isExcluded = false;
        @SuppressWarnings("unchecked")
        List<String> excludePatterns = holder.getExcludePatterns();
        if (excludePatterns != null && excludePatterns.size() > 0) {
            for (String excludePattern : excludePatterns) {
                int wildcardLen = 0;
                if (excludePattern.endsWith("**")) {
                    wildcardLen = 2;
                } else if (excludePattern.endsWith("*")) {
                    wildcardLen = 1;
                }
                if (wildcardLen > 0) {
                    excludePattern = excludePattern.substring(0,excludePattern.length() - wildcardLen);
                }
                if ((wildcardLen==0 && uri.equals(excludePattern)) || (wildcardLen > 0 && uri.startsWith(excludePattern))) {
                    isExcluded = true;
                    break;
                }
            }
        }
        return isExcluded;
    }

    private boolean areFileExtensionsEnabled() {
        if (grailsConfig != null) {
            final Boolean value = grailsConfig.getProperty(WebUtils.ENABLE_FILE_EXTENSIONS, Boolean.class);
            if (value != null) {
                return value;
            }
        }
        return true;
    }

    private boolean noRegexMappings(UrlMappingsHolder holder) {
        for (UrlMapping mapping : holder.getUrlMappings()) {
            if (mapping instanceof RegexUrlMapping) {
                return false;
            }
        }
        return true;
    }

    private boolean noControllers() {
        GrailsClass[] controllers = application.getArtefacts(ControllerArtefactHandler.TYPE);
        return controllers == null || controllers.length == 0;
    }

    private boolean noComposers() {
        GrailsClass[] composers = application.getArtefacts(ComposerArtefactHandler.TYPE);
        return composers == null || composers.length == 0;
    }

    private void checkDevelopmentReloadingState(HttpServletRequest request) {
        while(Environment.isReloadInProgress()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (request.getAttribute(GrailsExceptionResolver.EXCEPTION_ATTRIBUTE) != null) return;
        MultipleCompilationErrorsException compilationError = Environment.getCurrentCompilationError();
        if (compilationError != null) {
            throw compilationError;
        }
        Throwable currentReloadError = Environment.getCurrentReloadError();
        if (currentReloadError != null) {
            throw new RuntimeException(currentReloadError);
        }
    }

    protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
        // Lookup from request attribute. The resolver that handles MultiPartRequest is dealt with earlier inside DefaultUrlMappingInfo with Grails
        HttpServletRequest resolvedRequest = (HttpServletRequest) request.getAttribute(MultipartHttpServletRequest.class.getName());
        if (resolvedRequest != null) return resolvedRequest;
        return request;
    }

    private boolean renderViewForUrlMappingInfo(HttpServletRequest request, HttpServletResponse response, UrlMappingInfo info, String viewName) {
        if (viewResolver != null) {
            View v;
            try {
                // execute pre handler interceptors
                for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    if (!handlerInterceptor.preHandle(request, response, this)) return false;
                }

                // execute post handlers directly after, since there is no controller. The filter has a chance to modify the view at this point;
                final ModelAndView modelAndView = new ModelAndView(viewName);
                for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    handlerInterceptor.postHandle(request, response, this, modelAndView);
                }

                // TODO: verify
                v = WebUtils.resolveView(request, info.getViewName(), modelAndView.getViewName(), viewResolver);
                v.render(modelAndView.getModel(), request, response);

                // after completion
                for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    handlerInterceptor.afterCompletion(request, response, this, null);
                }
            }
            catch (Throwable e) {
                // let the sitemesh filter re-run for the error
                reapplySitemesh(request);
                for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
                    try {
                        handlerInterceptor.afterCompletion(request, response, this, e instanceof Exception ? (Exception)e : new GroovyPagesException(e.getMessage(), e));
                    }
                    catch (Exception e1) {
                        UrlMappingException ume = new UrlMappingException("Error executing filter after view error: " + e1.getMessage() + ". Original error: " + e.getMessage(), e1);
                        filterAndThrow(ume);
                    }
                }
                UrlMappingException ume = new UrlMappingException("Error mapping onto view [" + viewName + "]: " + e.getMessage(), e);
                filterAndThrow(ume);
            }
        }
        return true;
    }

    private void filterAndThrow(UrlMappingException ume) {
        filterer.filter(ume, true);
        throw ume;
    }

    private void reapplySitemesh(HttpServletRequest request) {
        request.removeAttribute("com.opensymphony.sitemesh.APPLIED_ONCE");
    }

    private void processFilterChain(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            WrappedResponseHolder.setWrappedResponse(response);
            if (filterChain != null) {
                filterChain.doFilter(request,response);
            }
        }
        finally {
            WrappedResponseHolder.setWrappedResponse(null);
        }
    }

    protected void createStackTraceFilterer() throws LinkageError {
        try {
            String className = application.getConfig().getProperty(
                "grails.logging.stackTraceFiltererClass", DefaultStackTraceFilterer.class.getName());
            filterer = (StackTraceFilterer) ClassUtils.forName(className, ClassUtils.getDefaultClassLoader()).newInstance();

        }
        catch (Throwable t) {
            logger.error("Problem instantiating StackTracePrinter class, using default: " + t.getMessage());
            filterer = new DefaultStackTraceFilterer();
        }
    }
}
