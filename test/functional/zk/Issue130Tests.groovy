package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue130Tests extends GebReportingTest {

    @Test
    void testForwardProperty() {
        browser.go "/zk/issue_130.zul"
        $('#comp_2').click()
        waitFor { $('#comp_2').text() == 'Clicked' }
        $('#btnForwardButton2').click()
        waitFor { $('#btnForwardButton2').text() == 'Clicked' }
    }

}
