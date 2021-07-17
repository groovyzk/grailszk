package jquery

import zk.User

class Form001Composer extends zk.grails.Composer {

    def afterCompose = { wnd ->
        def u = new User(name: "test", lastName: "last")
        $('#form').link(u)
        $('#btnSave').on('click', {
            def o = $('#form').object()
            $('#result').val(o.toString())
        })
    }

}
