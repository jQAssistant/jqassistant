def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id == 'default' }
def encodingConcept = defaultGroup.concept.find { it.@id == 'encoding' }
def rows = encodingConcept.result.rows
assert  rows.@count == 1
def value = rows.row[0].column.find{it.@name='umlauts'}.value
assert value == 'ÄÖÜß'
assert new File(basedir, 'target/site/jqassistant.html').exists()
assert new File(basedir, 'target/custom-site/jqassistant.html').exists()
assert !new File(basedir, 'target/surefire-reports/TEST-default.xml').exists()
