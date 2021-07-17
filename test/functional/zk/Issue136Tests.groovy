package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue136Tests extends GebReportingTest {

    @Test
    void testWrapHTML() {
        browser.go "/zk/issue136"
        waitFor { $('#comp_4').text().trim() == "ciao" }
    }

}
