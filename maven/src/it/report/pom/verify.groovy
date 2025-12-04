def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find{ it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'test:Pom' }.result.rows.@count == 2
assert defaultGroup.concept.find { it.@id == 'test:Pom' }.result.rows.row[0].column[0].value == "com.buschmais.jqassistant:jqassistant-maven-plugin.integration.singlemodule.pom"
