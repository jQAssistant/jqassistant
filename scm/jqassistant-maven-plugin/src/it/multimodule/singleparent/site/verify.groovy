def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find{ it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'metric:Top10TypesPerArtifact' }.result.rows.@count == 2
assert new File(basedir, 'target/site/jqassistant.html').exists()