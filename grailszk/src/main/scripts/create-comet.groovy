import grails.codegen.model.Model

description("Creates a new Comet") {
    usage "grails create-comet [COMET NAME]"
    argument name: "Comet Name", description: "The name of the comet to create"
    flag name: "force", description: "Whether to overwrite existing files"
}

String ARTIFACT_NAME = "Comet"
def name = args[0] - ARTIFACT_NAME + ARTIFACT_NAME
def overwrite = flag("force") as boolean

Model _model = model(name)

render(
    template: template("artifacts/${ARTIFACT_NAME}.groovy"),
    destination: file("grails-app/comets/${_model.packagePath}/${_model.className}.groovy"),
    model: _model,
    overwrite: overwrite
)

createUnitTest(_model.fullName, overwrite ? "--force" : "")
