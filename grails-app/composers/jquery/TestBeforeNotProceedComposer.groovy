package jquery

class TestBeforeNotProceedComposer extends zk.grails.Composer {

    def afterCompose = { wnd ->

        before("#delete") {
            return false
        }

        before('#delete') {
            $('#result').val('set by before advice')
        }

        $('#delete').on('click', {
            $('#result').val('never set')
        })

    }

}
