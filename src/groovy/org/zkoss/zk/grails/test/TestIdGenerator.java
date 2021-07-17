package org.zkoss.zk.grails.test;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.metainfo.*;
import org.zkoss.zk.ui.sys.IdGenerator;

public class TestIdGenerator implements IdGenerator {

    private static String ID_NUM = "$ID_$NUM_";

    private String nextUuid(Desktop desktop) {
        int i = Integer.parseInt(desktop.getAttribute(ID_NUM).toString());
        i++;// Start from 1
        desktop.setAttribute(ID_NUM, String.valueOf(i));
        return "comp_" + i;
    }

    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo info) {
        String id = comp.getId();
        if(id == null)    return nextUuid(desktop);
        if(id.equals("")) return nextUuid(desktop);

        return id;
    }

    public String nextDesktopId(Desktop desktop) {
        if (desktop.getAttribute(ID_NUM) == null) {
            String number = "0";
            desktop.setAttribute(ID_NUM, number);
        }
        return null;
    }

    public String nextPageUuid(Page page) {
        return null;
    }

}