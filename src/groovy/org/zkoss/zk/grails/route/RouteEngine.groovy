package org.zkoss.zk.grails.route

class RouteEngine {

    def patterns = []
    def closures = [:]

    def hashtag(pattern, closure) {
        def compileResult = compile(pattern)
        patterns.add(0, compileResult)
        closures[compileResult[0]] = closure
    }

    def process(url) {
        def result = match(url)
        if(result) {
            def (params, closure) = result
            def binding = new Binding()
            binding.setVariable("params", params)
            closure.delegate = binding
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            def values = params.values()
            if(values.size() == 1) {
                return closure.call(values[0])
            } else {
                return closure.call(params.values() as Object[])
            }
        }
        return null
    }

    def match(url) {
        def matchedResult = patterns.findResult { p ->
            def pattern = p[0]
            def keys    = p[1]
            def match   = url =~ pattern
            if(match) {
                [match, keys, closures[pattern]]
            } else {
                null
            }
        }
        if(matchedResult) {
            def (match, keys, closure) = matchedResult
            def params = [:]
            match[0][1..-1].eachWithIndex { v, i ->
                params[keys[i]] = v
            }
            return [params, closure]
        }
        return null
    }

    def compile(path) {
        assert path[0] == "/"
        def keys = []

        def segments = path.substring(1).split('/').collect { segment ->

            def pattern = segment
            pattern.replaceAll(/((:\w+)|\*)/) { match ->
                if(match == "*") {
                    keys << "splat"
                    "(.*?)"
                } else {
                    keys << match[2][1..-1]
                    "(\\w+)"
                }
            }

        }

        //
        // if the segment is optional, then its prefix / will be also optional
        //
        def result = segments.collect {
            if(it[-1] == "?") "/?$it" else "/$it"
        }.join()

        //
        // added \A and \z to mark begin and end of input
        ///
        ["\\A${result}\\z", keys]
    }

}