assert new File(basedir, 'site/target/customReport.txt.1').exists()
assert new File(basedir, 'site/target/customReport.txt.2').exists()

def reportFile = new File(basedir, 'site/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)
def testValueConcept1 = report.group.concept.find { it.@id == "customPlugin:testValue1" }
assert testValueConcept1 != null
assert testValueConcept1.result.rows['@count'] == "1";
assert testValueConcept1.result.rows.row[0].column[0].value == "testValue";

def testValueConcept2 = report.group.concept.find { it.@id == "customPlugin:testValue2" }
assert testValueConcept2 == ""
