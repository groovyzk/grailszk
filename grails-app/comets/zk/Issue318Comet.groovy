package zk

class Issue318Comet extends zk.grails.Comet {

    int i = 0

    static trigger = [startDelay: 800L, delay: 1000L]

    def execute = { desktop, page ->
        i++
        lblTest.value = "zk.grails.Comet : ${i}"

        if(i == 3) stop()
    }
}
