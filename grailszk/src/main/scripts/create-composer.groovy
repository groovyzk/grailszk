import grails.codegen.model.Model

description("Creates a new Composer") {
    usage "grails create-composer [COMPOSER NAME]"
    argument name: "Composer Name", description: "The name of the composer to create"
    flag name: "force", description: "Whether to overwrite existing files"
}

String ARTIFACT_NAME = "Composer"
def name = args[0] - ARTIFACT_NAME + ARTIFACT_NAME
def overwrite = flag("force") as boolean

Model _model = model(name)

render(
    template: template("artifacts/${ARTIFACT_NAME}.groovy"),
    destination: file("grails-app/composers/${_model.packagePath}/${_model.className}.groovy"),
    model: _model,
    overwrite: overwrite
)

createUnitTest(_model.fullName, overwrite ? "--force" : "")
