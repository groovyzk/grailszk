package zk.grails.aop;

import org.zkoss.zk.ui.Component;

public interface ComposerInterceptor {
    public void apply(Component comp);
}
