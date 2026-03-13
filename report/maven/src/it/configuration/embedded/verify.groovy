assert !new File(basedir, "target/jqassistant/store").exists()
verifyProfile("yaml")
verifyProfile("properties")

private void verifyProfile(profileName) {
// config from plugin execution configuration (embedded YAML)
    assert new File(basedir, "target/custom-store-" + profileName).exists()

// config from project configuration (.jqassistant.yml)
    def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report-' + profileName + '.xml')
    assert reportFile.exists()
    def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
    assert jqassistantReport.group.size() == 2
}
