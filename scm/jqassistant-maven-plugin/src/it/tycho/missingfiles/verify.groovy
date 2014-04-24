try {
    def log = "Scanning"
    def log2 = "META-INF/MANIFEST.MF"
    def logFile = new File(basedir, "build.log")
    def found = false
	
	logFile.eachLine({ line ->
        if(line.contains(log) && line.contains(log2)) {
            found = true
        }
    });

	assert !found
    return true
} catch(Throwable e) {
    e.printStackTrace()
    return false
}