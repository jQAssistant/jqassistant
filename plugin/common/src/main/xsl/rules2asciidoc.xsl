<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="/">
        =
        <xsl:call-template name="filename"/>
        <xsl:value-of select="$newline"/>
        == Constraints
        <xsl:apply-templates select="//constraint">
            <xsl:sort select="@id" order="ascending"/>
        </xsl:apply-templates>
        == Concepts
        <xsl:apply-templates select="//concept">
            <xsl:sort select="@id" order="ascending"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="constraint | concept">
        ===
        <xsl:value-of select="@id"/>
        <xsl:value-of select="$newline"/>
        Requires concepts:
        <xsl:for-each select="requiresConcept">
            *
            <xsl:value-of select="@refId"/><xsl:value-of select="$newline"/>
        </xsl:for-each>
        <xsl:value-of select="$newline"/>
        <xsl:value-of select="description"/>
        <xsl:value-of select="$newline"/>
        [source,cypher]
        ----
        <xsl:value-of select="cypher"/>
        ----
    </xsl:template>

    <xsl:template name="filename">
        <xsl:variable name="tokenized">
            <xsl:value-of select="tokenize(base-uri(), '/')[last()]"/>
        </xsl:variable>
        <xsl:value-of select="$tokenized"/>
    </xsl:template>

</xsl:stylesheet>
