def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }

verifyCount(defaultGroup, 'test:MainArtifactDependency')
verifyCount(defaultGroup, 'test:TestArtifactDependency')
verifyCount(defaultGroup, 'test:TypeDependency')

private void verifyCount(defaultGroup, String conceptId) {
    def row = defaultGroup.concept.find { it.@id == conceptId }.result.rows.row[0]
    assert row.column.find { it.@name == 'count' }.value == 1
}
