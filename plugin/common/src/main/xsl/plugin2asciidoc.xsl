<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:plugin="http://www.buschmais.com/jqassistant/core/plugin/schema/v1.0">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <xsl:param name="artifactId"/>

    <xsl:variable name="pluginName">
        <xsl:value-of select="*[local-name()='jqassistant-plugin'][0]/@name"/>
    </xsl:variable>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="plugin:jqassistant-plugin">
= <xsl:value-of select="@name"/> Plugin
<xsl:value-of select="$newline"/>
        <xsl:apply-templates select="//description"/>
<xsl:value-of select="$newline"/>
        <xsl:apply-templates select="//scanner"/>

        <xsl:if test="//resource">
include::{docRoot}/<xsl:value-of select="$artifactId"/>/concepts-and-constraints.adoc[]
        </xsl:if>
        <xsl:apply-templates select="//report"/>
        <xsl:apply-templates select="//model"/>
    </xsl:template>

    <xsl:template match="description">
        <xsl:value-of select="$newline"/>
        <xsl:value-of select="text()"/>
    </xsl:template>

    <!-- Finding a scanner element is taken as signal that
     !   there is a written documentation in a file scanner.adoc
     !   Yes, this is questionable.
     !   Oliver B. Fischer, 2016-02-26
     !-->
    <xsl:template match="scanner">
        <xsl:value-of select="$newline"/>
include::{docRoot}/<xsl:value-of select="$artifactId"/>/scanner.adoc[]
    </xsl:template>

    <xsl:template match="model">

== Model of the Plugin

        <xsl:value-of select="$newline"/>
Refer to the link:javadoc/<xsl:value-of select="$artifactId"/>/index.html[plugin Javadoc] for details
about the model.
    </xsl:template>

    <xsl:template match="report">
        <xsl:value-of select="$newline"/>
include::{docRoot}/<xsl:value-of select="$artifactId"/>/report.adoc[]
    </xsl:template>

</xsl:stylesheet>
