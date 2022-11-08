assert !new File(basedir, 'site/target/customReport.txt.1').exists()
assert new File(basedir, 'site/target/customReport.txt.2').exists()

// Asciidoc report from  plugin (with includes)
def asciidocReportFile = new File(basedir, 'site/target/jqassistant/report/asciidoc/custom/index.html')
assert asciidocReportFile.exists()
String asciidocReport = asciidocReportFile.getText("UTF-8")
assert asciidocReport.contains("<img src=\"../../plantuml/custom_ClassDiagram.svg\">")
assert asciidocReport.contains("<img src=\"../../plantuml/custom_ComponentDiagram.svg\">")
assert asciidocReport.contains('<a href="../../csv/custom_CSVReport.csv"')
assert new File(basedir, 'site/target/jqassistant/report/plantuml/custom_ClassDiagram.svg').exists()
assert new File(basedir, 'site/target/jqassistant/report/plantuml/custom_ComponentDiagram.svg').exists()
assert new File(basedir, 'site/target/jqassistant/report/csv/custom_CSVReport.csv').exists()
assert new File(basedir, 'site/target/jqassistant/report/asciidoc/custom-embedded-plantuml.svg').exists()

// XML report
def reportFile = new File(basedir, 'site/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)
assert report.group.concept.find { it.@id == 'customPlugin:PluginURI' }.status == "success"
verifyValueConcept(report.group.concept.find { it.@id == "customPlugin:testValue1" })
verifyValueConcept(report.group.concept.find { it.@id == "customPlugin:testValue2" })


private void verifyValueConcept(testValueConcept) {
    assert testValueConcept != null
    assert testValueConcept.result.rows['@count'] == "1";
    assert testValueConcept.result.rows.row[0].column[0].value == "testValue";
}

