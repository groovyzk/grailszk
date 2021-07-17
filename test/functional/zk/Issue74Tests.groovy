package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue74Tests extends GebReportingTest {

    @Test
    void testSession() {
        browser.go "/zk/issue_74.zul"
        // user label
        waitFor {
            $('#comp_4').text() == "mock user"
        }
        waitFor {
            $('#comp_5').text() == "mock user 2"
        }
    }

}
