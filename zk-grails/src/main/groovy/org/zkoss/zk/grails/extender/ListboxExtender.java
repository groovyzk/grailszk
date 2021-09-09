package org.zkoss.zk.grails.extender;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;

public class ListboxExtender {

    public static void setModel(Object delegate, ListModel p) {
        Listbox listbox = (Listbox)delegate;
        if(listbox.isMultiple() == true) {
            if(p instanceof ListModelList) {
                ((ListModelList)p).setMultiple(true);
            }
        }
        listbox.setModel(p);
    }

    public static ListModel getModel(Object delegate) {
        Listbox listbox = (Listbox)delegate;
        return listbox.getModel();
    }

}
