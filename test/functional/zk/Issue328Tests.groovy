package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue328Tests extends GebReportingTest {

    @Test
    void testSession() {
        browser.go "/zk/issue328/index.zul"
        //
        // sub page must be loaded and initialized correctly without NPE
        //
        waitFor {
            $('#comp_4').text() == "Sub"
        }
    }

}
