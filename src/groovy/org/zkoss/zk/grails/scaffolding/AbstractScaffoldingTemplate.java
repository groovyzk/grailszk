package org.zkoss.zk.grails.scaffolding;

import grails.persistence.Event;

import org.apache.commons.lang.StringUtils;

import org.codehaus.groovy.grails.commons.GrailsApplication;

import org.zkoss.zk.grails.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zkplus.databind.DataBinder;

import org.zkoss.zk.grails.select.JQuery;

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