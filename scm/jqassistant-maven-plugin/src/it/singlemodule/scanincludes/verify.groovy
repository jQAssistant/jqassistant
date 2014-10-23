def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)
def testValueConcept = report.group.concept.find { it.@id == "scanInclude:Properties" }
assert testValueConcept != null
assert testValueConcept.result.rows['@count'] == "1";
assert testValueConcept.result.rows.row[0].column[0].value == "/test.properties";
