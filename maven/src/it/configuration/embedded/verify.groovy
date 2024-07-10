assert !new File(basedir, "target/jqassistant/store").exists()
// config from plugin execution configuration (embedded YAML)
assert new File(basedir, "target/custom-store").exists()

// config from project configuration (.jqassistant.yml)
def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
assert jqassistantReport.group.size() == 2
