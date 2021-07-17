package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class HelloViewModelTests extends GebReportingTest {

    @Test
    void testHelloViewModel() {
        browser.go "/zk/hello_vm"
        $('#comp_3').click()
        waitFor {
            $('#comp_2').text() == 'hello'
        }
    }

}
