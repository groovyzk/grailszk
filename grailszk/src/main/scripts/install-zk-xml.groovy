description("Copy the default zk.xml file to the main app at webapp/WEB-INF/") {
    usage "grails install-zk-xml"
    flag name: "force", description: "Whether to overwrite existing files"
}

def overwrite = flag('force') as boolean

render(
    template: "conf/zk/zk.xml",
    destination: file("src/main/webapp/WEB-INF/zk.xml"),
    overwrite: overwrite
)
