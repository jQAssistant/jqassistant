<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jqa-report="http://www.buschmais.com/jqassistant/core/report/schema/v1.0">
    <xsl:output method="html" version="1.0" encoding="iso-8859-1" indent="yes"/>
    <xsl:include href="/xsl/jqassistant-report.xsl" />
    <xsl:template match="/">
        <xsl:call-template name="content" />
    </xsl:template>

</xsl:stylesheet>
