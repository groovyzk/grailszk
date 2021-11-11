import grails.codegen.model.Model

description("Creates a new LiveModel") {
    usage "grails create-live-model [LIVE MODEL NAME]"
    argument name: "Live Model Name", description: "The name of the live model to create"
    flag name: "force", description: "Whether to overwrite existing files"
}

String ARTIFACT_NAME = "LiveModel"
def name = args[0] - ARTIFACT_NAME + ARTIFACT_NAME
def overwrite = flag("force") as boolean

Model _model = model(name)

render(
    template: template("artifacts/${ARTIFACT_NAME}.groovy"),
    destination: file("grails-app/livemodels/${_model.packagePath}/${_model.className}.groovy"),
    model: _model.asMap() + [
        domainName: _model.className - ARTIFACT_NAME
    ],
    overwrite: overwrite
)

createUnitTest(_model.fullName, overwrite ? "--force" : "")
