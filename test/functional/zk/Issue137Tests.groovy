package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue137Tests extends GebReportingTest {

    @Test
    void testComet() {
        browser.go "/zk/issue_137.zul"
        waitFor { $('#comp_4').text() == "Ready" }
        waitFor { $('#comp_5').text() == "Ready" }
        waitFor { $('#comp_6').text() == "Ready" }
        $('#comp_2').click()
        waitFor { $('#comp_4').text() == "Before" }
        waitFor { $('#comp_5').text() == "time : 1" }
        waitFor { $('#comp_5').text() == "time : 2" }
        waitFor { $('#comp_6').text() == "After" }
    }

}
