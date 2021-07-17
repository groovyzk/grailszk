package jquery

import org.junit.Test

class Form001Tests extends zk.grails.ComposerTestCase {

    @Test
    void "test form binding"() {
        browser.go "/zk/jquery/form_001_test.zul"
        waitFor { jq('#txtName').value() == "test" }
        waitFor { jq('#txtLastName').value() == "last" }

        jq('#txtName').value("test2")
        jq('#txtLastName').value("last2")

        jq('#btnSave').click()

        waitFor { jq('#result').text() == "test2 last2" }
    }
}
