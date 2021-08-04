package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue327Tests extends GebReportingTest {

    @Test
    void testSession() {
        browser.go "/zk/issue_327.zul"
        // the first row is multiple selectable with checkbox
        waitFor {
            $('#comp_37-cm').hasClass('z-listitem-checkable')
            $('#comp_37-cm').hasClass('z-listitem-checkbox')
        }
    }

}
