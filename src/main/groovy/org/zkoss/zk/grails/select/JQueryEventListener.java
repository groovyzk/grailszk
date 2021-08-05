package org.zkoss.zk.grails.select;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import java.util.List;

public class JQueryEventListener implements EventListener<Event> {

    private Closure<?> handler;

    public Closure<?> getHandler() {
        return handler;
    }

    public void setHandler(Closure<?> handler) {
        this.handler = handler;
    }

    public void onEvent(Event e) throws Exception {
        Component target = e.getTarget();
        boolean proceedTheEvent = true;
        if(target != null) {
            List<Closure> beforeList = (List<Closure>)target.getAttribute("$JQ_BEFORE$", Component.COMPONENT_SCOPE);
            if(beforeList != null) {
                for(Closure before : beforeList) {
                    try {
                        Object result = InvokerHelper.invokeClosure(before, new Object[]{e});
                        if(result instanceof Boolean) {
                            proceedTheEvent = proceedTheEvent && (Boolean)result;
                        }
                    } catch (Exception ex) {
                        throw ex;
                    }
                }
            }
        }

        if(!proceedTheEvent) {
            return;
        }

        try {
            handler.setDelegate(target);
            InvokerHelper.invokeClosure(handler, new Object[]{e});
            handler.setDelegate(null);
        } catch(Exception ex) {
            throw ex;
        }

        if(target != null) {
            List<Closure> afterList = (List<Closure>)target.getAttribute("$JQ_AFTER$", Component.COMPONENT_SCOPE);
            if(afterList != null) {
                for(Closure after: afterList) {
                    try {
                        InvokerHelper.invokeClosure(after, new Object[]{e});
                    } catch (Exception ex) {
                        throw ex;
                    }
                }
            }
        }
    }

}
