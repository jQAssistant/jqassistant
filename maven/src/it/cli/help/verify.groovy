def outputFile = new File(basedir, "target/invoker/cli/help/build.log")
assert outputFile.exists()
def logContent = outputFile.text
assert logContent.contains("Display help information on jqassistant-maven-plugin.").status == "success"
