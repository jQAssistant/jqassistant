assert new File(basedir, 'site/target/customReport.txt').exists()
def reportFile = new File(basedir, 'site/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
//def report = new XmlSlurper().parse(reportFile)
//def testValueConcept = report.find { it.@id == "customplugin:testValue" }
//#assert testValueConcept != null
//assert testValueConcept.result.rows['@count'] == "1";
//assert testValueConcept.result.rows.row[0].column[0].value == "testValue";
