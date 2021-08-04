package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class Issue219Tests extends GebReportingTest {

    @Test
    void testURLMapping() {
        browser.go "/zk/hello"
        // user label
        waitFor { $('#comp_3').text() == "Hello World" }
        $('#comp_4').click()
        waitFor { $('#comp_3').text() == "Hello" }
    }

}
