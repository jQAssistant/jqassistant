<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <xsl:param name="pluginName"/>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="/">
=== <xsl:value-of select="$pluginName"/>
<xsl:apply-templates select="//description"/>
<xsl:apply-templates select="//resource"/>
    </xsl:template>

    <xsl:template match="description">
        <xsl:value-of select="$newline"/>
        <xsl:value-of select="text()"/>
    </xsl:template>

    <xsl:template match="resource">
include::{docRoot}/<xsl:value-of select="$pluginName"/>/<xsl:value-of select="text()"/>.adoc[]
<xsl:value-of select="$newline"/>
    </xsl:template>
</xsl:stylesheet>
