package test

/**
 * Extend from another class beside GrailsComposer and zk.grails.Composer
 */
class Issue317Composer extends NoGrailsExtendComposer {

    def afterCompose = {

        $("#ok").label("Loaded #1")

    }

}