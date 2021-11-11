import org.grails.io.support.Resource
import org.grails.io.support.SpringIOUtils

description("Copy all templates to be used for resource creation. All create-* commands use these templates") {
    usage "grails install-templates-zk-grails"
    flag name: "force", description: "Whether to overwrite existing files"
}

boolean force = flag("force")

templates("artifacts/**/*").each { Resource r ->
    String path = r.URL.toString().replaceAll(/^.*?META-INF/, "src/main")
    if (!path.endsWith("/")) {
        File to = new File(path)
        if (!to.exists() || force) {
            to.getParentFile().mkdirs()
            SpringIOUtils.copy(r, to)
            println("Copied ${r.filename} to ${to.canonicalPath}")
        }
    }
}
