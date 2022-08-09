package org.zkoss.zk.grails.api;

import org.zkoss.zk.ui.Component;

public abstract class AbstractComposersApi {
    public abstract Component getRoot(final Object p0);
    
    public abstract void setRoot(final Object p0, final Object p1);
    
    public abstract Object $(final Object p0, final String p1);
    
    public abstract Object $(final Object p0, final Object[] p1);
    
    public abstract Object $d(final Object p0, final String p1);
}
