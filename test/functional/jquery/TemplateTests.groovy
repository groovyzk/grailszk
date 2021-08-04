package jquery

import org.junit.Test

class TemplateTests extends zk.grails.ComposerTestCase {

    @Test
    void "test template binding"() {
        browser.go "/zk/jquery/template_test.zul"

        waitFor { $('#comp_9').value() == "test 1" }
        waitFor { $('#comp_14').value() == "test 2" }

        $('a.z-paging-next').click()

        waitFor { $('#comp_9').value() == "test 3" }
        waitFor { $('#comp_14').value() == "test 4" }
    }

}
