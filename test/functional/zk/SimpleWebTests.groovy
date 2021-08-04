package zk

import geb.junit4.GebReportingTest
import org.junit.Ignore
import org.junit.Test

class SimpleWebTests extends GebReportingTest {

    @Test
    void testNonPackageComposer() {
        browser.go "/zk/non_package_composer_tests.zul"
        $("#comp_3").click()
        waitFor { $('#comp_3').text() == "clicked" }
    }

    @Test
    void testPackageComposer() {
        browser.go "/zk/test/index.zul"

        $('#comp_9').value("test")
        waitFor { $('#comp_9').value() == "test" }

        $('#comp_10').click()
        waitFor { $('#comp_9').value() == "done" }

        $('#comp_14 > input[type="checkbox"]').click()
        waitFor { $('#comp_14').hasClass('strike') == true }

        $('#comp_14 > input[type="checkbox"]').click()
        waitFor { $('#comp_14').hasClass('strike') == false }
    }

}
