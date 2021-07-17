package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class HashTagTests extends GebReportingTest {

    @Test
    void testActionViaTag() {
        browser.go "/zk/hello#world"
        waitFor {
            $('#comp_3').text() == "Hello World via Tag"
        }
    }

    @Test
    void testActionViaTagWithParam() {
        browser.go "/zk/hello#world/me"
        waitFor {
            $('#comp_3').text() == "Hello World via Tag with who = me"
        }
    }
}
