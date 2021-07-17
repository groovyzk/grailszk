package org.zkoss.zk.grails;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Page;

public class MessageHolder {

    private MessageSource messageSource;

    public MessageHolder(Page page) {
        super();
        ServletContext servletContext = page.getDesktop().getWebApp().getServletContext();
        ApplicationContext ctx  = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        messageSource = (MessageSource)ctx.getBean("messageSource");
        if(messageSource == null) {
            throw new BeanCreationException("No bean [messageSource] found in ApplicationContext");
        }
    }

    public String getAt(String code) {
        Locale lc = Locales.getCurrent();
        return messageSource.getMessage(code, null, lc);
    }

    public String call(Map<?,?> map) {

        ObjectError error = (ObjectError)map.get("error");
        String code = (String)map.get("code");
        List<?> args   = (List<?>)map.get("args");

        if(error != null) {
            String text;
            try {
                text = messageSource.getMessage(error, Locales.getCurrent());
            } catch (NoSuchMessageException e) {
                if (error instanceof MessageSourceResolvable) {
                    text = error.getCode();
                } else {
                    text = error.toString();
                }
            }
            return text;
        } else {
            Object[] arr = args.toArray(new Object[args.size()]);
            return  messageSource.getMessage(code, arr, Locales.getCurrent());
        }
    }
}
