package jquery

import org.junit.Test

class NestedPropTests extends zk.grails.ComposerTestCase {

    @Test
    void testNestedProp() {
        browser.go "/zk/jquery/databinding_nested_prop_tests.zul"
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
        waitFor { $('#comp_10').text() == "user.name: aaa, str: changed, bool: false, int: 15, date: Mon Jan 30 00:12:00 ICT 2012, double: 22.5" }
    }

}
