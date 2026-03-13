def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

assert buildLog.getText().contains("Invalid rule configuration")
assert buildLog.getText().contains("unknown-group")
