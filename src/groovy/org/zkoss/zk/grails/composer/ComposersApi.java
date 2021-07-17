package org.zkoss.zk.grails.composer;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.grails.select.JQuery;
import org.zkoss.zk.ui.select.Selectors;
import java.util.*;
import org.zkoss.zk.grails.api.AbstractComposersApi;

public class ComposersApi extends AbstractComposersApi {

    private Component root;

    public void setRoot(Component value) { this.root = value; }
    public Component getRoot() { return this.root; }

    public Component getRoot(Object composer) {
        return root;
    }

    public void setRoot(Object composer, Object root) {
        this.root = (Component)root;
    }

    public Object $(Object composer, String arg) {
        return JQuery.select(root, new Object[]{arg});
    }

    public Object $(Object composer, Object[] args) {
        return JQuery.select(root, args);
    }

    public Object $d(Object composer, String arg)    {
        Desktop desktop = root.getDesktop();
        Collection<Component> results = new HashSet<Component>();
        for(Page p : desktop.getPages()) {
            for(Component r : p.getRoots()) {
                results.addAll(Selectors.find(r, arg));
            }
        }
        return new JQuery(new ArrayList<Component>(results));
    }

}