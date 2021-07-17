package zk.grails.aop;

import groovy.lang.Closure;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;

import java.util.ArrayList;
import java.util.List;

public class Weaver {

    public static enum AdviceKind {
        BEFORE,
        AFTER
    }

    public static void weave(Component root, String query, AdviceKind kind, Closure c) {
        List<Component> results = Selectors.find(root, query);
        for (Component comp: results) {
            final String adviceKind = "$JQ_" + kind.toString() + "$";
            if(comp.hasAttribute(adviceKind, Component.COMPONENT_SCOPE)) {
                List<Closure> closureList = (List<Closure>) comp.getAttribute(adviceKind, Component.COMPONENT_SCOPE);
                closureList.add(c);
                comp.setAttribute(adviceKind, closureList, Component.COMPONENT_SCOPE);
            } else {
                List<Closure> closureList = new ArrayList<Closure>();
                closureList.add(c);
                comp.setAttribute(adviceKind, closureList, Component.COMPONENT_SCOPE);
            }
        }
    }

    public static void unweave(Component root, String query, AdviceKind kind) {
        List<Component> results = Selectors.find(root, query);
        for (Component comp: results) {
            comp.removeAttribute("$JQ_" + kind.toString() + "$", Component.COMPONENT_SCOPE);
        }
    }

}
