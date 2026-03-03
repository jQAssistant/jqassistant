def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new groovy.xml.XmlSlurper().parse(reportFile)

verifyConcept(report, "test:ProjectHasModel", 1)
// Both raw POM and effective model have root=true (Maven 4 compat Model provides it for both)
verifyConcept(report, "test:PomHasRootFlag", 2)
verifyConcept(report, "test:ProfileHasConditionActivation", 2)

def verifyConcept(report, conceptId, expectedRowCount) {
    def conceptResult = report.group.concept.find { it.@id == conceptId }
    assert conceptResult != null : "Concept ${conceptId} not found in report"
    assert conceptResult.result.rows['@count'] == Integer.toString(expectedRowCount) :
        "Expected ${expectedRowCount} rows for concept ${conceptId}, got ${conceptResult.result.rows['@count']}"
}
