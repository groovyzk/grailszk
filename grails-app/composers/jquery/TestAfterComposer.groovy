package jquery

class TestAfterComposer extends zk.grails.Composer {

    def afterCompose = {
        after('#delete') {
            $('#result2').val('set by after advice')
        }

        $('#delete').on('click', {
            $('#result1').val('this is set by handler')
        })
    }

}
