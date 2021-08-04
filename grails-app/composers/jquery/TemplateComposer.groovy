package jquery

import zk.User

class TemplateComposer extends zk.grails.Composer {

    def afterCompose = { window ->

        //
        // define a template for listbox(#list)
        // 5 columns
        //
        $('#list').template {
            listitem {
                listcell { textbox(inplace: true) }
                listcell { textbox(inplace: true) }
            }
        }

        def all = [ new User(name: "test 1", lastName: "last 1"),
                    new User(name: "test 2", lastName: "last 2"),
                    new User(name: "test 3", lastName: "last 3"),
                    new User(name: "test 4", lastName: "last 4")
                  ]

        //
        // declare an adhoc method, named "scaffold"
        // for $('#list')
        //
        $('#list').prototype.scaffold = { domainClass, size ->

            def $listbox = $(delegate)

            def fields = $listbox.find("listheader")*.value
            def bindings = fields.collectEntries { f ->
                [(f): "listcell:eq(${fields.indexOf(f)}) *"]
            }
            println bindings

            def results = all[0..1]
            $listbox.fill(results, "listitem", bindings)

            def $paging = $listbox.siblings("paging")
            $paging pageSize: size, totalSize: 4
            $paging.on("paging", {
                def offset = $(it).activePage()*2
                $listbox.fill(all[offset..offset+size-1])
            })
        }

        //
        // use the declared adhoc method
        //
        $('#list').scaffold(User, 2)

    }

}