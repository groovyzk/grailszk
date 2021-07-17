package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue142Tests extends GebReportingTest {

    @Test
    void testComet() {
        browser.go "/zk/issue_142.zul"
        waitFor { $('#comp_4').text() == "Ready" }
        $('#comp_2').click()
        waitFor { $('#comp_5').text() == "time : 2" }
        $('#comp_3').click() // stop
        $('#comp_2').click() // start
        waitFor { $('#comp_5').text() == "time : 4" }
        $('#comp_3').click() // stop
        waitFor {
            def i = ($('#comp_5').text() - 'time : ').toInteger()
            i >= 4
        }
    }

}
