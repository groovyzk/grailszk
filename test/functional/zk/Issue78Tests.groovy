package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue78Tests extends GebReportingTest {

    @Test
    void testZulResponseWrapper() {
        // a controller: TestController.index
        // with views/test/index.gsp
        // and z:head, z:body
        browser.go "/zk/test"
        // image
        waitFor {
            $('#comp_4').attr('src').startsWith("http://localhost:8080/zk/ext/images/grails_logo.png")
        }
        // button
        waitFor {
            $('#comp_10').text() == 'add'
        }
    }

}
