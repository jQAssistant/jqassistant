def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def mavenModules = report.group.concept.find { it.@id == "test:MavenModules" }
assert mavenModules != null
assert mavenModules.result.rows['@count'] == "2";
