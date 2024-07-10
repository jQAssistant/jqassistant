def junitReportFile = new File(basedir, 'target/jqassistant/report/junit/TEST-jqassistant.Group_nestedGroup.xml')
assert junitReportFile.exists()
def testsuiteNode = new groovy.xml.XmlSlurper().parse(junitReportFile)
assert testsuiteNode.testcase.size() >= 3
def memberByType = testsuiteNode.testcase.find { it.@id= 'Constraint_test_MemberByType'}
assert memberByType.failure.message != null



