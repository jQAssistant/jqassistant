<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="/">
<xsl:value-of select="$newline"/>
        <xsl:if test="//concept">
===== Concepts
            <xsl:apply-templates select="//concept">
                <xsl:sort select="@id" order="ascending"/>
            </xsl:apply-templates>
        </xsl:if>
        <xsl:if test="//constraint">
===== Constraints
            <xsl:apply-templates select="//constraint">
                <xsl:sort select="@id" order="ascending"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="constraint | concept">
[id="<xsl:value-of select="@id"/>"]
====== <xsl:value-of select="@id"/>
<xsl:value-of select="$newline"/>

        <xsl:if test="deprecated">
            <xsl:value-of select="$newline"/>
            _The rule is deprecated: <xsl:value-of select="deprecated"/>_
            <xsl:value-of select="$newline"/>
        </xsl:if>

        <xsl:if test="requiresConcept">
Requires concepts:
        <xsl:for-each select="requiresConcept">
* &lt;&lt;<xsl:value-of select="@refId"/>&gt;&gt;<xsl:value-of select="$newline"/>
        </xsl:for-each>
    </xsl:if>
<xsl:value-of select="$newline"/>
<xsl:value-of select="description"/>
<xsl:value-of select="$newline"/>

        [source,cypher,indent=0]
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
