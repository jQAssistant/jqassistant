<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:plugin="http://www.buschmais.com/jqassistant/core/plugin/schema/v1.0">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <xsl:param name="pluginName"/>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="plugin:jqassistant-plugin">
=== <xsl:value-of select="@name"/>
<xsl:value-of select="$newline"/>
<xsl:apply-templates select="//description"/>
<xsl:value-of select="$newline"/>
<xsl:value-of select="$newline"/>
<xsl:for-each select="//resource">&lt;&lt;<xsl:value-of select="text()"/>&gt;&gt; </xsl:for-each>
<xsl:apply-templates select="//scanner"/>
<xsl:apply-templates select="//resource"/>
    </xsl:template>

    <xsl:template match="description">
        <xsl:value-of select="$newline"/>
        <xsl:value-of select="text()"/>
    </xsl:template>

    <xsl:template match="scanner">
        <xsl:value-of select="$newline"/>
include::{docRoot}/<xsl:value-of select="$pluginName"/>/scanner.adoc[]
    </xsl:template>

    <xsl:template match="resource">
[[<xsl:value-of select="text()"/>]]
==== <xsl:value-of select="text()"/>
include::{docRoot}/<xsl:value-of select="$pluginName"/>/<xsl:value-of select="text()"/>.adoc[]
<xsl:value-of select="$newline"/>
    </xsl:template>
</xsl:stylesheet>
