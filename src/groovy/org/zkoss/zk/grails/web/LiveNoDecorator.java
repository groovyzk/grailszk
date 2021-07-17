package org.zkoss.zk.grails.web;

import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.sitemesh.Content;
import com.opensymphony.sitemesh.webapp.SiteMeshWebAppContext;
import com.opensymphony.sitemesh.webapp.decorator.BaseWebAppDecorator;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;
import org.codehaus.groovy.grails.web.util.StreamCharBuffer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

public class LiveNoDecorator extends BaseWebAppDecorator implements Decorator {

    private LinkGenerator grailsLinkGenerator;

    public LiveNoDecorator(LinkGenerator grailsLinkGenerator) {
        this.grailsLinkGenerator = grailsLinkGenerator;
    }

    @Override
    protected void render(Content content, HttpServletRequest request, HttpServletResponse response,
                          ServletContext servletContext, SiteMeshWebAppContext webAppContext)
            throws IOException, ServletException {

        StringWriter strWriter = new StringWriter();
        content.writeOriginal(strWriter);
        strWriter.flush();
        String original = strWriter.toString();
        String contextPath = request.getContextPath();
        if(original.indexOf("src=\""+ contextPath + "/zkau/") > 0) {
            String link = grailsLinkGenerator.resource(new HashMap(){{
                put("dir","ext/js");
                put("file","z-it-live.js");
                put("plugin", "zk");
            }});
            link = link.replaceAll("/plugins", "/static/plugins");
            original = original.replace("</head>", "\n<script type=\"text/javascript\" src=\"" + link + "\" charset=\"UTF-8\"></script>\n</head>");
        }

        if (webAppContext.isUsingStream()) {
            // http://jira.opensymphony.com/browse/SIM-196 , skip setting setContentLength
            //response.setContentLength(content.originalLength());
            OutputStream output=response.getOutputStream();
            PrintWriter writer = new PrintWriter(output);
            writer.write(original);
            writer.flush();
        } else {
            PrintWriter writer = response.getWriter();
            writer.write(original);
            writer.flush();
        }
    }

    public String getPage() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getURIPath() {
        return null;
    }

    public String getRole() {
        return null;
    }

    public String getInitParameter(String paramName) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Iterator getInitParameterNames() {
        return null;
    }
}
