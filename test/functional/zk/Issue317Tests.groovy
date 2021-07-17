package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue317Tests extends GebReportingTest {

    @Test
    void doTest() {
        browser.go "/zk/issue_317.zul"
        assert $('#comp_2').text() == "Loaded #1"
    }

}