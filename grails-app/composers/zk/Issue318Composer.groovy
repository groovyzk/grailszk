package zk

class Issue318Composer extends zk.grails.Composer {

    def lblTest
    def issue318Comet

    def onClick_btnStart() {
        println "start"
        issue318Comet.start()
    }

}
