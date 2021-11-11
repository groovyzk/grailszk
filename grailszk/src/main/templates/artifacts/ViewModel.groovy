package ${packageName}

import org.zkoss.zk.grails.*
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.select.annotation.Wire

class ${className} {

    String message
    @Wire  btnHello

    @Init init() {
        // initialzation code here
    }

    @NotifyChange(['message'])
    @Command clickMe() {
        message = "Clicked"
    }

}
