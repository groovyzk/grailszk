package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue336Tests extends GebReportingTest {

    @Test
    void testGettingListboxModel() {
        browser.go "/zk/issue_336.zul"
        // the first row is multiple selectable with checkbox
        waitFor {
            $('#comp_37-cm').hasClass('z-listitem-checkable')
            $('#comp_37-cm').hasClass('z-listitem-checkbox')
        }
    }

}
