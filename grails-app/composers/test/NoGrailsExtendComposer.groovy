package test

class NoGrailsExtendComposer extends zk.grails.Composer {

    def afterCompose = {

        $("#ok").label("Loaded")

    }

}