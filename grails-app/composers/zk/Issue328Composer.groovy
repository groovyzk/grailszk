package zk

import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Vbox
import org.zkoss.zul.Window

class Issue328Composer extends zk.grails.Composer {

    def afterCompose = { Window wnd ->
        Vbox vbox = new Vbox()
        Window window = (Window) Executions.createComponents("sub.zul", vbox, [:])
        println "Desktop: ${window.getDesktop()}"
        vbox.setParent(wnd)
    }

}
