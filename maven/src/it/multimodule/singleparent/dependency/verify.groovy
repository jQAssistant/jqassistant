def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
def row = defaultGroup.concept.find { it.@id == 'test:Dependency' }.result.rows.row[0]
assert row.column.find { it.@name == 'count' }.value == 1


