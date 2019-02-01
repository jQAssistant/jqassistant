def reportFile = new File(basedir, 'target/jqassistant/report/junit/TEST-jqassistant.Group_nestedGroup.xml')
assert reportFile.exists()
def testsuiteNode = new XmlSlurper().parse(reportFile)
assert testsuiteNode.testcase.size() >= 2
