package org.zkoss.zk.grails.scaffolding;

import grails.core.GrailsApplication;
import org.zkoss.zk.ui.Component;

public interface ScaffoldingTemplate {

    String SCAFFOLDING_TEMPLATE = "zkgrailsScaffoldingTemplate";

    void initComponents(Class<?> scaffoldClass,
                        Component window,
                        GrailsApplication grailsApplication);

}
