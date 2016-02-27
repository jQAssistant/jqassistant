<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <!-- We need the literal name of the plugin. It can be found in the
     !   jqassistant-plugin.xml document. This is the reason for
     !   this XSL foo.
     !   Oliver B. Fischer, 2016-02-25
     !-->
    <xsl:param name="dirOfPluginXML"/>
    <xsl:variable name="fullPathOfPluginXML">
        <xsl:value-of select="$dirOfPluginXML"/>
        <xsl:value-of select="'/'"/>
        <xsl:value-of select="'jqassistant-plugin.xml'"/>
    </xsl:variable>
    <xsl:variable name="pluginDoc" select="doc($fullPathOfPluginXML)"/>

    <xsl:variable name="pluginName">
        <xsl:value-of select="$pluginDoc/*[local-name()='jqassistant-plugin']/@name"/>
    </xsl:variable>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="/">
<xsl:value-of select="$newline"/>
        <xsl:if test="//concept">
=== Provided concepts by the <xsl:value-of select="$pluginName"/> plugin
            <xsl:apply-templates select="//concept">
                <xsl:sort select="@id" order="ascending"/>
            </xsl:apply-templates>
        </xsl:if>
        <xsl:if test="//constraint">
=== Provided constraints by the <xsl:value-of select="$pluginName"/> plugin
            <xsl:apply-templates select="//constraint">
                <xsl:sort select="@id" order="ascending"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="constraint | concept">

        <xsl:variable name="typeName">
            <xsl:choose>
                <xsl:when test="local-name(.) = 'constraint'">Constraint</xsl:when>
                <xsl:when test="local-name(.) = 'concept'">Concept</xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Element
                        <xsl:value-of select="local-name(.)"/>
                        not supported.
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

[id="<xsl:value-of select="@id"/>"]
==== <xsl:value-of select="$typeName"/>&#x00A0;<xsl:value-of select="@id"/>
<xsl:value-of select="$newline"/>

        <xsl:if test="deprecated">
            <xsl:value-of select="$newline"/>
            _The rule is deprecated: <xsl:value-of select="deprecated"/>_
            <xsl:value-of select="$newline"/>
        </xsl:if>

        <xsl:if test="requiresConcept">
Required concepts:
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
