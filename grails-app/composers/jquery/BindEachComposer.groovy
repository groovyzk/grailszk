package jquery

class BindEachComposer extends zk.grails.Composer {

    def afterCompose = {

        $('#combo').each(m: ['1','2','3']).append {
            comboitem(label: m)
        }

        $('#result').val($('#combo > comboitem').size())

    }

}