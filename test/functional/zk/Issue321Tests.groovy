package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue321Tests extends GebReportingTest {

    @Test
    void testSession() {
        browser.go "/zk/issue_321.zul"
        // user label
        waitFor {
            def sessionId = $('#comp_4').text()
            sessionId.length() == 32
            sessionId.toCharArray().every { ch -> (ch in ('A'..'F')) || (ch in ('0'..'9')) }
        }
    }

}
