package jquery

import org.zkoss.zk.grails.select.JQuery

class ParentComposer extends zk.grails.Composer {

    def afterCompose = { wnd ->
        $('#btn').text('Fail')
        def p = $('#btn').parent()
        assert p instanceof JQuery
        assert p[0].id == wnd.id
        $('#btn').text('Passed')
    }

}
