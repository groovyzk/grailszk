package zk.hello

import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Button

class HelloViewModel {

    String message = "default text"

    @Wire('#btnHello') Button btnHello

    @NotifyChange(['message'])
    @Command buttonClicked() {
        message = "hello"
        btnHello.label = "clicked"
    }

}
