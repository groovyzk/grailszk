package zk

import geb.junit4.GebReportingTest
import org.junit.Ignore
import org.junit.Test

class Issue139Tests extends GebReportingTest {

    @Ignore
    @Test
    void testComet() {
        browser.go "/zk/issue_139.zul"
        waitFor { $('#comp_4').text() == "Ready" }
        waitFor { $('#comp_5').text() == "Ready" }
        waitFor { $('#comp_6').text() == "Ready" }
        waitFor { $('#comp_7').text() == "Ready" }
        waitFor { $('#comp_8').text() == "Ready" }

        $('#comp_2').click()

        final timeout = 60.0
        waitFor(timeout) { $('#comp_4').text() == "begin" }
        waitFor(timeout) { $('#comp_4').text() == "time1 : 1" }
        waitFor(timeout) { $('#comp_4').text() == "time1 : 2" }

        waitFor(timeout) { $('#comp_4').text() == "end" }
        waitFor(timeout) { $('#comp_5').text() == "end" }
        waitFor(timeout) { $('#comp_6').text() == "end" }
        waitFor(timeout) { $('#comp_7').text() == "end" }
        waitFor(timeout) { $('#comp_8').text() == "end" }
    }

}
