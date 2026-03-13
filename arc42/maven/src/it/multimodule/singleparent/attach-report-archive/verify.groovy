assert new File(basedir, "target/jqassistant/jqassistant-report.zip").exists()
def buildLog = new File(basedir, 'build.log')
def foundInstallingReport = false
buildLog.eachLine {
    foundInstallingReport = foundInstallingReport || verifyInstallingReport(it)
}
assert foundInstallingReport

static boolean verifyInstallingReport(String line) {
    line ==~ /^\[INFO] Installing .*-jqassistant-report.zip$/
}
