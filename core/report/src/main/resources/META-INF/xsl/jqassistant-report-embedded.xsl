<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:import href="/META-INF/xsl/jqassistant-report.xsl" />
	<xsl:output method="html" version="1.0" encoding="iso-8859-1"
		indent="yes" />
	<xsl:template match="/">
		<xsl:call-template name="content" />
	</xsl:template>

</xsl:stylesheet>
