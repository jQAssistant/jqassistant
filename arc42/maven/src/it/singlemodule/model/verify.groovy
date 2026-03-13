def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new groovy.xml.XmlSlurper().parse(reportFile)

verifyConcept(report, "test:ProjectHasModel", 1)
verifyConcept(report, "test:ProjectHasEffectiveModel", 1)
verifyConcept(report, "test:TestArtifactDependsOnMainArtifact", 1)

def verifyConcept(report, conceptId, expectedRowCount) {
    def conceptResult = report.group.concept.find { it.@id == conceptId }
    assert conceptResult != null
    assert conceptResult.result.rows['@count'] == Integer.toString(expectedRowCount);

}
