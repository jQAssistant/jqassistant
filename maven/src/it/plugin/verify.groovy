assert !new File(basedir, 'site/target/customReport.txt.1').exists()
assert new File(basedir, 'site/target/customReport.txt.2').exists()

def reportFile = new File(basedir, 'site/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)
verifyValueConcept(report.group.concept.find { it.@id == "customPlugin:testValue1" })
verifyValueConcept(report.group.concept.find { it.@id == "customPlugin:testValue2" })

private void verifyValueConcept(testValueConcept) {
    assert testValueConcept != null
    assert testValueConcept.result.rows['@count'] == "1";
    assert testValueConcept.result.rows.row[0].column[0].value == "testValue";
}

