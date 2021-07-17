package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue318Tests extends GebReportingTest {

    @Test
    void testComet() {
        browser.go "/zk/issue_318_test_comet.zul"
        waitFor {
            $('#comp_3').text() == "Ready"
        }
        $('#comp_2').click()
        waitFor { $('#comp_3').text() == 'zk.grails.Comet : 1' }
        waitFor { $('#comp_3').text() == 'zk.grails.Comet : 2' }
        waitFor { $('#comp_3').text() == 'zk.grails.Comet : 3' }
    }

}
