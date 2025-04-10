File buildLog = new File(basedir, "build.log")
assert buildLog.exists()
assert buildLog.text.contains("Display help information on jqassistant-maven-plugin.")
