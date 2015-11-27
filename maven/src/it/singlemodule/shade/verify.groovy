def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)
def testConcept = report.group.concept.find { it.@id == "test:ProjectHasModel" }
assert testConcept != null
assert testConcept.result.rows['@count'] == "1";
