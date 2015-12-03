def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)

def hasModelConcept = report.group.concept.find { it.@id == "test:ProjectHasModel" }
assert hasModelConcept != null
assert hasModelConcept.result.rows['@count'] == "1";

def hasEffectiveModelConcept = report.group.concept.find { it.@id == "test:ProjectHasEffectiveModel" }
assert hasEffectiveModelConcept != null
assert hasEffectiveModelConcept.result.rows['@count'] == "1";
