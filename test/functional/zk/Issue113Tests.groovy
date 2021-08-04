package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue113Tests extends GebReportingTest {

    @Test
    void test_UTF8_Display() {
        browser.go "/zk/issue_113.zul"
        waitFor {
            $('#comp_2').text() ==  "版本1.0"
        }
        waitFor {
            $("#comp_3").text() == "说明"
        }
    }

}
