package zk

import org.zkoss.zul.ListModelList

class Issue327Composer extends zk.grails.Composer {

    def afterCompose = { wnd ->
        def model = []
        (1..50).each { i ->
            model << "${i}"
        }
        $('#testListbox').model = new ListModelList(model)
    }

}
