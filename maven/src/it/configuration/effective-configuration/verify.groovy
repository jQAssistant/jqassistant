def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()
assert buildLog.getText().contains("""    continue-on-error: true""")
