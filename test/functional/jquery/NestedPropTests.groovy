package jquery

import org.junit.Test

class DotObjectTests extends zk.grails.ComposerTestCase {

    @Test
    void testBeforeNotProceed() {
        browser.go "/zk/jquery/databinding_dot_object_tests.zul"
        $('#comp_80').value('')
        $('#comp_80') << 'changed'
        $('#comp_82 > input[type="checkbox"]').click()
        $('#comp_84.z-intbox').value('')
        $('#comp_84.z-intbox') << '15'
        $('#comp_86-real').value('')
        $('#comp_86-real') << '30 ม.ค. 2012'
        $('#comp_88.z-doublebox').value('')
        $('#comp_88.z-doublebox') << "22.5"
        $('#comp_12').click()
        $('#comp_77').click()
        waitFor { $('#comp_10').text() == "id: 6, str: changed, bool: false, int: 15, date: Mon Jan 30 00:12:00 ICT 2012, double: 22.5" }
    }

}
