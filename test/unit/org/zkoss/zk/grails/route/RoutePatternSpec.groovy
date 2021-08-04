package org.zkoss.zk.grails.route

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

class RoutePatternSpec extends Specification {

    def "compile test"() {
        given:
        RouteEngine r = new RouteEngine()

        when:
        def (result, keys) = r.compile("/home/:id")

        then:
        result == "\\A/home/(\\w+)\\z"
        keys   == ["id"]
    }

    def "compile optional"() {
        given:
        RouteEngine r = new RouteEngine()

        when:
        def (result, keys) = r.compile("/home/:id/:action?")

        then:
        result == "\\A/home/(\\w+)/?(\\w+)?\\z"
        keys   == ["id", "action"]
    }

    def "single pattern test"() {
        given:
        RouteEngine r = new RouteEngine()
        def c = { id -> "id = $id" }
        r.hashtag("/home/:id", c)

        when:
        def (params, closure) = r.match("/home/1")

        then:
        params["id"] == "1"
        closure == c
    }

    def "multi pattern test"() {
        given:
        RouteEngine r = new RouteEngine()
        def c1 = { id -> "id = $id" }
        r.hashtag("/home/:id", c1)
        def c2 = { id, blogId -> "id = $id, blogId = $blogId"}
        r.hashtag("/blog/:id/:blogId", c2)

        when:
        def (params, closure) = r.match("/blog/ck/1234")

        then:
        params["id"] == "ck"
        params["blogId"] == "1234"
        closure == c2
    }

    def "multi pattern process"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id") { id ->
            "id = $id"
        }

        r.hashtag("/blog/:id/:blogId") { id, blogId ->
            "id = $id, blogId = $blogId"
        }

        when:
        def result = r.process("/blog/ck/1234")

        then:
        result == "id = ck, blogId = 1234"
    }

    def "multi pattern process with params"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id") { id ->
            "id = $id"
        }

        r.hashtag("/blog/:id/:blogId") {
            "id = ${params['id']}, blogId = ${params['blogId']}"
        }

        when:
        def result = r.process("/blog/ck/1234")

        then:
        result == "id = ck, blogId = 1234"
    }

    def "test not match"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id") { id ->
            "id = $id"
        }

        r.hashtag("/blog/:id/:blogId") {
            "id = ${params['id']}, blogId = ${params['blogId']}"
        }

        when:
        def result = r.process("/notfound")

        then:
        result == null
    }

    def "test optional not existed"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id/:action?") { id, action ->
            "id = $id, action = $action"
        }

        when:
        def result = r.process("/home/1")

        then:
        result == "id = 1, action = null"
    }

    def "test optional existed"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id/:action?") { id, action ->
            "id = $id, action = $action"
        }

        when:
        def result = r.process("/home/1/test")

        then:
        result == "id = 1, action = test"
    }

    def "test precedance matching"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id") { id ->
            "A: id = $id"
        }
        r.hashtag("/home/:id/:action") { id, action ->
            "B: id = $id, action = $action"
        }

        when:
        def result1 = r.process("/home/1/test")
        def result2 = r.process("/home/1")

        then:
        result1 == "B: id = 1, action = test"
        result2 == "A: id = 1"
    }

    def "test the end of input"() {
        given:
        RouteEngine r = new RouteEngine()
        r.hashtag("/home/:id") { id ->
            "A: id = $id"
        }

        when:
        def result = r.process("/home/1/x")

        then:
        result == null
    }

}