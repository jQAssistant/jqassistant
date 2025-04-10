def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new groovy.xml.XmlSlurper().parse(reportFile)

assert report.context.build.name == "Custom Build"
assert report.context.build.properties.find { it.@key = 'Branch' } == "develop"

