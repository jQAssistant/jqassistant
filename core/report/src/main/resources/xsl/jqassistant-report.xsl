<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jqa-report="http://www.buschmais.com/jqassistant/core/report/schema/v1.0">
    <xsl:output method="html" version="1.0" encoding="iso-8859-1" indent="yes"/>
    <xsl:template match="/">
        <html>
            <head>
                <title>jQAssistant Report</title>
            </head>
            <body>
                <h1>jQAssistant Report</h1>
                <xsl:apply-templates select="*/jqa-report:constraintGroup"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="jqa-report:constraintGroup">
        <div>
            <h2>
                <xsl:value-of select="@id"/>
            </h2>
        </div>
        <div>
            <h3>Constraints</h3>
        </div>
        <div>
            <xsl:apply-templates select="jqa-report:constraint"/>
        </div>
        <div>
            <h3>Concepts</h3>
        </div>
        <div>
            <xsl:apply-templates select="jqa-report:concept"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:constraint">
        <div>
            <h4>
                <xsl:value-of select="@id"/>
            </h4>
        </div>
        <div>
            <xsl:apply-templates select="jqa-report:result"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:concept">
        <div>
            <h4>
                <xsl:value-of select="@id"/>
            </h4>
        </div>
        <div>
            <xsl:apply-templates select="jqa-report:result"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:result">
        <div>
            <xsl:apply-templates select="jqa-report:rows"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:rows">
        <div>
            <xsl:apply-templates select="jqa-report:row"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:row">
        <div>
            <xsl:apply-templates select="jqa-report:column"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:column">
        <div>
            <xsl:value-of select="."/>
        </div>
    </xsl:template>

</xsl:stylesheet>
