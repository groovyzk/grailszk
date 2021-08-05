package zk.grails.aop;

import org.zkoss.zk.ui.Component;

public interface ComposerInterceptor {
    void apply(Component comp);
}
