package zk

import org.zkoss.zul.ListModelList

class Issue336Composer extends zk.grails.Composer {

    def testListbox

    def afterCompose = { wnd ->
        def model = []
        (1..50).each { i ->
            model << "${i}"
        }
        testListbox.model = new ListModelList(model)
        try {
            def myModel = testListbox.model
            testListbox.model = myModel
        } catch(e) {
            alert(e.toString())
        }
        testListbox.invalidate()
    }

}
