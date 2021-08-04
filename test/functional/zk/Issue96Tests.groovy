package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue96Tests extends GebReportingTest {

    @Test
    void testZulTagWithBorderLayoutAsFirstNode() {
        browser.go "/zk/issue96"
        // z-borderlayout
        waitFor {
            $('#comp_1').hasClass('z-borderlayout')
        }
    }

}
