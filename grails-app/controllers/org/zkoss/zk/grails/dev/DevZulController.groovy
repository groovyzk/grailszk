package org.zkoss.zk.grails.dev

class DevZulController {

    def devHolder

    //
    // index?r=/index.zul
    //
    def index() {
        if(devHolder == null) {
            response.sendError(200)
            return
        }

        synchronized (devHolder) {
            def f = devHolder.check('/zul' + params['r'])
            if(f) {
                response.setDateHeader('Last-Modified', f.lastModified())
                response.sendError(200)
            } else {
                response.sendError(200)
            }
        }
    }

}