package org.zkoss.zk.grails.artefacts;

import grails.core.ArtefactHandlerAdapter;
import org.grails.core.artefact.DomainClassArtefactHandler;


public class CometArtefactHandler extends ArtefactHandlerAdapter {

    public static final String TYPE = "Comet";

    public CometArtefactHandler() {
        super(TYPE, GrailsCometClass.class,
            DefaultGrailsCometClass.class,
            DefaultGrailsCometClass.COMET,
            false);
    }

    public boolean isArtefactClass(@SuppressWarnings("rawtypes") Class clazz) {
        return super.isArtefactClass(clazz) &&
               !DomainClassArtefactHandler.isDomainClass(clazz);
    }

}
