def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)

verifyConcept(report, "test:APOC")
verifyConcept(report, "test:GraphAlgorithms")

def verifyConcept(report, conceptId) {
    def conceptResult = report.group.concept.find { it.@id == conceptId }
    assert conceptResult != null
    assert conceptResult.status == "success";
}
