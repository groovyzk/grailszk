package zk.grails;

import groovy.lang.Closure;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.springframework.context.ApplicationContext;
import org.zkoss.zk.grails.composer.GrailsComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.spring.SpringUtil;
import zk.grails.aop.ComposerInterceptor;
import zk.grails.aop.Weaver;

import java.util.Map;

public class Composer extends GrailsComposer {

    public void before(String query, Closure advice) {
        Weaver.weave(getRoot(), query, Weaver.AdviceKind.BEFORE, advice);
    }

    public void after(String query, Closure advice) {
        Weaver.weave(getRoot(), query, Weaver.AdviceKind.AFTER, advice);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        //
        // Support generic composer interceptors
        //
        ApplicationContext ctx = SpringUtil.getApplicationContext();
        GrailsApplication app = ctx.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);
        ApplicationContext mainContext = app.getMainContext();
        Map<String, ComposerInterceptor> beans = mainContext.getBeansOfType(ComposerInterceptor.class);
        for(ComposerInterceptor ci : beans.values()) {
            ci.apply(comp);
        }

    }

}
