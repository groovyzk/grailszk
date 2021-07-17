package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue125Tests extends GebReportingTest {

    @Test
    void testComet() {
        browser.go "/zk/issue_125_test_comet.zul"
        waitFor {
            $('#comp_3').text() == "Ready"
        }
        $('#comp_2').click()
        waitFor { $('#comp_3').text() == 'time : 1' }
        waitFor { $('#comp_3').text() == 'time : 2' }
        waitFor { $('#comp_3').text() == 'time : 3' }
    }

}
