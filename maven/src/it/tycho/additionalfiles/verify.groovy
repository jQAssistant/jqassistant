def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new XmlSlurper().parse(reportFile)
assert report.group.constraint.size() == 1
