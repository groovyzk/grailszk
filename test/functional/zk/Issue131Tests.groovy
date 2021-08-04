package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue131Tests extends GebReportingTest {

    @Test
    void testMessage() {
        browser.go "/zk/issue_131.zul"
        waitFor { $('#comp_2').text() == "ทดสอบข้อความ" }
        $("#comp_2").click()
        waitFor { $("#comp_2").text() == "คลิ๊ก" }
        $("#comp_3").click()
        waitFor { $("#comp_3").text() == "คลิ๊ก test" }
    }

}
