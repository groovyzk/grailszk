package org.zkoss.zk.grails.scaffolding;

import grails.core.GrailsApplication;
import org.zkoss.zk.grails.select.JQuery;
import org.zkoss.zk.ui.Component;

public abstract class AbstractScaffoldingTemplate implements ScaffoldingTemplate {

    protected Class<?>  scaffold;
    protected Component window;
    protected GrailsApplication application;

    protected JQuery $(String arg)    { return JQuery.select(window, new Object[]{arg});   }
    protected JQuery $(Object[] args) { return JQuery.select(window, args); }

    public void initComponents( Class<?> scaffold,
                                Component window,
                                GrailsApplication grailsApplication) {

        this.scaffold = scaffold;
        this.window = window;
        this.application = grailsApplication;

    }

}
