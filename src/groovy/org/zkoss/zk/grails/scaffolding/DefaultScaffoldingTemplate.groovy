package org.zkoss.zk.grails.scaffolding

import grails.persistence.Event

import org.apache.commons.lang.StringUtils as SU

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.validation.DomainClassPropertyComparator

import org.zkoss.zk.grails.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.ForwardEvent
import org.zkoss.zkplus.databind.BindingListModelList
import org.zkoss.zkplus.databind.DataBinder

import org.zkoss.zk.grails.select.JQuery

import java.util.List

import java.text.SimpleDateFormat;


class DefaultScaffoldingTemplate extends AbstractScaffoldingTemplate {

    def scaffoldProps
    def listboxProps
    def rowProps

    def redraw(page = 0) {
        def dc = application.getDomainClass(scaffold.name)

        def pageSize = $("#pag${dc.name}")[0].pageSize
        def list = scaffold.list(offset: page * pageSize, max: pageSize)
        $("#lst${dc.name} > listitem").detach()
        $("#lst${dc.name}").append {
            list.each { e ->
                listitem(value: e) {
                    listboxProps.each { p ->
                        if(!p.isAssociation() || p.oneToOne || p.manyToOne) {
                            def content = e[p.name]
                            if(p.type == Date.class) {
                                content = org.zkoss.text.DateFormats.format(content, false)
                            }
                            listcell(id:"cell${p.name}${e.id}", label:"${content}")
                        } else {
                            listcell(id:"cell${p.name}${e.id}", label:"#${e[p.name]?.size()}")
                        }
                    }
                }
            }
        }
    }

    private editor(Class type) {
        switch(type) {
            case String.class:
            case URL.class:
                return "textbox"

            case Integer.class:
            case int.class:
                return "intbox"

            case Long.class:
            case long.class:
                return "longbox"

            case Double.class:
            case Float.class:
            case double.class:
            case float.class:
                return "doublebox"

            case java.util.Date.class:
            case java.sql.Date.class:
            case Calendar.class:
                return "datebox"

            case java.sql.Time.class:
                return "timebox"

            case Boolean.class:
            case boolean.class:
                return "checkbox"

            default:
                return null
        }

        return null
    }

    static PAGE_SIZE = 8

    public void initComponents( Class<?> scaffold,
                                Component window,
                                GrailsApplication grailsApplication) {

        def dc = grailsApplication.getDomainClass(scaffold.name)
        def placeHolder = window.getFellowIfAny("scaffoldingBox")
        if (!placeHolder) return

        super.initComponents(scaffold, placeHolder, grailsApplication)

        //
        // Hack to make vbox filled the parent
        //
        placeHolder.width = "100%"

        scaffoldProps = dc.properties as List
        Collections.sort(scaffoldProps, new DomainClassPropertyComparator(dc))

        listboxProps = scaffoldProps.inject([]) { result, p ->
            def cp = dc.constrainedProperties[p.name]
            if(cp?.display == true && result.size() < 6) {
                result << p
            }
            return result
        }
        rowProps = scaffoldProps.inject([]) { result, p ->
            def cp = dc.constrainedProperties[p.name]
            if(cp?.display) {
                result << [p, cp]
            }
            return result
        }

        placeHolder.append {
            listbox(id: "lst${dc.name}", multiple: true, rows: PAGE_SIZE) {
                listhead {
                    listboxProps.each { p ->
                        if(!p.isAssociation()) {
                            //
                            // sorting
                            //
                            listheader(label:"${p.naturalName}")
                        } else {
                            listheader(label:"${p.naturalName}")
                        }
                    }
                }
            }
            paging(id: "pag${dc.name}", pageSize: PAGE_SIZE)
            separator()
            groupbox {
                caption(label: "${dc.name}")
                toolbar {
                    def w="75px"
                    toolbarbutton(id:"btnAdd",     width: w, image: resource('images', 'skin/database_add.png'),    label:"New")
                    toolbarbutton(id:"btnUpdate",  width: w, image: resource('images', 'skin/database_save.png'),   label:"Update")
                    toolbarbutton(id:"btnDelete",  width: w, image: resource('images', 'skin/database_delete.png'), label:"Delete")
                    toolbarbutton(id:"btnRefresh", width: w, image: resource('images', 'skin/database_table.png'),  label:"Refresh")
                }
                separator()
                tabbox {
                    tabs {
                        tab(label:"All")
                        rowProps.each { p, cp ->
                            if( p.oneToMany || p.manyToMany ) {
                                if(p.association) {
                                    tab(label: "${p.naturalName}")
                                }
                            }
                        }
                    }
                    tabpanels {
                        tabpanel {
                            grid(id: "gd${dc.name}") {
                                columns(visible: false) {
                                    column(label: "Column", width: "150px")
                                    column(label: "Value")
                                }
                                rows {
                                    rowProps.each { p, cp ->
                                        def editorTag = this.editor(p.type)
                                        row {
                                            label(value: "${p.naturalName}:")
                                            if(editorTag) {
                                                "${editorTag}"(id: "fd_${p.name}", width: "12em")
                                            } else {

                                                if(p.manyToOne || p.oneToOne) {
                                                    if(p.association) {
                                                        bandbox(id:"bb_${p.name}", width: "12em", readonly: true) {
                                                            bandpopup(width: "300px") {
                                                                listbox(id: "fd_${p.name}", mold:"paging", autopaging: true,
                                                                    model: new BindingListModelList(new PageableGormList(p.type, PAGE_SIZE) as List, true))
                                                            }
                                                        }
                                                    }
                                                } else if( p.oneToMany || p.manyToMany ) {
                                                    if(p.association) {
                                                        listbox(id: "fd_${p.name}", mold: "paging", pagingPosition: "top", pageSize: PAGE_SIZE, width: "20em")
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        //
        // Bind object using rowProps
        //
        // #fd_ is an editor for a field
        // #bb_ is a bandbox, in case we are looking up through associations.
        //
        // details: "#fd_details"
        // details: [selector: "#fd_details", attr: "model"]
        //
        $("#gd${dc.name}").link(null,
            rowProps.inject([:]) { x, p ->
                if(p[0].manyToOne || p[0].oneToOne) {
                    x["${p[0].name}"] = "#fd_${p[0].name}, #bb_${p[0].name}"
                } else if( p[0].oneToMany || p[0].manyToMany ) {
                    x["${p[0].name}"] = [selector:"#fd_${p[0].name}", attr:"model"]
                } else {
                    x["${p[0].name}"] = "#fd_${p[0].name}"
                }
                return x
            }
        )

        $("bandbox > bandpopup > listbox").on('select', { ev ->
            def bandboxId = "bb_" + ($(ev)[0].id - "fd_") // convert fd_x to bb_x
            $("#${bandboxId}").val(ev.reference.value)
        })

        $("#gd${dc.name}").on('save', {
            $("#lst${dc.name}").binder().loadAll()
        })

        $("#lst${dc.name}").on('select', {
            def selectedCard = $(it).val()
            $("#gd${dc.name}").bind(selectedCard)

            def map = listboxProps.inject([:]) { result, p ->
                if(!p.isAssociation() || p.oneToOne || p.manyToOne) {
                    result[p.name] = "#cell${p.name}${selectedCard.id}"
                }
                return result
            }

            $("#lst${dc.name}")
                .unlink()
                .link(selectedCard, map)
        })

        $("#pag${dc.name}")[0].totalSize = scaffold.count()

        $("#pag${dc.name}").on('paging', { ev ->
            redraw(ev.activePage)
        })

        redraw()
    }

}