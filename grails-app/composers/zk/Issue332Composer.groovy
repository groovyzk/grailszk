package zk

import org.zkoss.zul.ListModelList
import org.zkoss.zul.Listbox

class Issue332Composer extends zk.grails.Composer {

    def testListbox

    def afterCompose = { wnd ->
        def model = []
        (1..50).each { i ->
            model << "${i}"
        }
        testListbox.model = new ListModelList(model)
        testListbox.invalidate()
    }

}
