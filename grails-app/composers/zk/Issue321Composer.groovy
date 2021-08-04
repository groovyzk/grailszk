package zk

import org.zkoss.zkgrails.*

class Issue321Composer extends org.zkoss.zk.grails.composer.GrailsComposer {
    
    def lblId

    def afterCompose = { window ->
        session['id'] = "mock id"
        lblId.value = session['id']
    }

}
