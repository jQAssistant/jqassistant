def reportFile1 = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile1.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile1)
def itGroup = jqassistantReport.group.find { it.@id = 'it' }
def concept = itGroup.concept.find { it.@id == 'it:APOCHelp' }
assert concept.status == 'success'

def pluginDir = new File(basedir, 'target/jqassistant/plugins')
assert pluginDir.exists();
assert pluginDir.isDirectory();
