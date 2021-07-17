package org.zkoss.zk.grails.composer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import groovy.lang.Closure;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.zkoss.zk.ui.Component;

public class WrapperGrailsComposer extends GrailsComposer {

    private static final long serialVersionUID = 2176200084257293417L;
    private static final Log LOG = LogFactory.getLog(WrapperGrailsComposer.class);
    private Object innerComposer;

    public WrapperGrailsComposer() {
        super();
    }

    public Object getInnerComposer() {
        LOG.debug(">>> getInnerComposer");
        return innerComposer;
    }

    public void setInnerComposer(Object innerComposer) {
        LOG.debug(">>> setInnerComposer");
        this.innerComposer = innerComposer;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        LOG.debug(">>> wrapper doAfterCompose");

        final Object composer = innerComposer;

        Object api = InvokerHelper.getProperty(composer, "instanceAbstractComposersApi");
        //
        // set root composer
        //
        InvokerHelper.setProperty(api, "root", comp);

        Object afterCompose = GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue(composer, "afterCompose");
        if(afterCompose != null) {
            //
            // there's afterCompose
            //
            if(afterCompose instanceof Closure) {
                InvokerHelper.invokeClosure(afterCompose, new Object[]{comp});
            }
        } else {
            //
            // no afterCompose closure
            // try method afterCompose()
            //
            InvokerHelper.invokeMethod(composer, "afterCompose", new Object[]{comp});
        }
    }

}
