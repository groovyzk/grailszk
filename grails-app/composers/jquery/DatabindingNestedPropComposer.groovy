package jquery

import zk.NestedProp
import zk.User

import java.text.SimpleDateFormat

class DatabindingNestedPropComposer extends zk.grails.Composer {

    def afterCompose = { wnd ->

        def list = []
        10.times {
            def obj = new NestedProp(
                user: new User(name: "aaa", lastName:"bbb"),
                dateField: new SimpleDateFormat('dd/mm/yyyy').parse("31/12/2012")
            )
            obj.id = it + 1
            list << obj
        }

        $('#lstMain').template {
            listitem {
                listcell { label()     }
                listcell { textbox()   }
                listcell { checkbox()  }
                listcell { intbox()    }
                listcell { datebox()   }
                listcell { doublebox() }
            }
        }

        $('#lstMain').fill(list, 'listitem', [
            'user.name':   'listcell:eq(0) label',
            'strField':    'listcell:eq(1) textbox',
            'boolField':   'listcell:eq(2) checkbox',
            'intField':    'listcell:eq(3) intbox',
            'dateField':   'listcell:eq(4) datebox',
            'doubleField': 'listcell:eq(5) doublebox'
        ])

        $('#lstMain').on('select', {
            NestedProp obj = $(it).object()
            def str = "user.name: ${obj.user.name}, str: ${obj.strField}, bool: ${obj.boolField}, int: ${obj.intField}, date: ${obj.dateField}, double: ${obj.doubleField}"
            $('#result').val(str)
        })

    }

}
