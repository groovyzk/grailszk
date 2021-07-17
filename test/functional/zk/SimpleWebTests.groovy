package zk

import geb.junit4.GebReportingTest
import org.junit.Ignore
import org.junit.Test

class SimpleWebTests extends GebReportingTest {

    @Ignore
    @Test
    void testNonPackageComposer() {
        browser.go "/zk/non_package_composer_tests.zul"
        $("#comp_3").click()
        waitFor { $('#comp_3').text() == "clicked" }
    }

    @Test
    void testPackageComposer() {
        browser.go "/zk/test/index.zul"

        $('#comp_12').value("test")
        waitFor { $('#comp_12').value() == "test" }

        $('#comp_13').click()
        waitFor { $('#comp_12').value() == "done" }

        $('#comp_17 > input[type="checkbox"]').click()
        waitFor { $('#comp_17').hasClass('strike') == true }

        $('#comp_17 > input[type="checkbox"]').click()
        waitFor { $('#comp_17').hasClass('strike') == false }
    }

}
