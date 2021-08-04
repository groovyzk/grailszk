package jquery

class PrototypeComposer extends zk.grails.Composer {

    def afterCompose = { w ->
        $('#combo').prototype.fillData = { ->
            $('#combo').append {
                comboitem(label: "First")
                comboitem(label: "Second")
            }
        }

        $('#combo').fillData()
    }
}
