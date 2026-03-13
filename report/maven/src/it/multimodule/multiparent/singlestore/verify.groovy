assert new File(basedir, 'target/jqassistant').exists()
assert !new File(basedir, 'module1/target/jqassistant').exists()
assert !new File(basedir, 'module2/target/jqassistant').exists()
