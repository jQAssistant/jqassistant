assert new File(basedir, 'parent/target/jqassistant').exists()
def reportFile = new File(basedir, 'parent/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find{ it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'integrationtest:TestClassName' }.result.rows.@count == 2
