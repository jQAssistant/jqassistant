<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exsl="http://exslt.org/common"
                extension-element-prefixes="exsl"
                xmlns:xs="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" version="1.0" encoding="utf8" indent="no"/>

    <xsl:param name="dirOfRules"/>

    <xsl:variable name="pluginName">
        <xsl:value-of select="/child::node()/@name"/>
    </xsl:variable>

    <xsl:variable name='newline'>
        <xsl:text>&#10;</xsl:text>
    </xsl:variable>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="/">

        <!-- Collect all concepts from all files in the directory
         !   $dirOfRules.
         !-->
        <xsl:variable name="allConcepts">
            <xsl:for-each select="//resource">
                <xsl:variable name="fqp2rulesFile">
                    <xsl:value-of select="$dirOfRules"/>
                    <xsl:value-of select="'/'"/>
                    <xsl:value-of select="text()"/>
                </xsl:variable>

                <xsl:variable name="content" select="document($fqp2rulesFile)"/>
                <xsl:variable name="concepts" select="$content/*"/>

                <xsl:for-each select="$concepts/concept">
                    <xsl:copy copy-namespaces="no">
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="allConstraints">
            <xsl:for-each select="//resource">
                <xsl:variable name="fqp2rulesFile">
                    <xsl:value-of select="$dirOfRules"/>
                    <xsl:value-of select="'/'"/>
                    <xsl:value-of select="text()"/>
                </xsl:variable>

                <xsl:variable name="content" select="document($fqp2rulesFile)"/>
                <xsl:variable name="constraints" select="$content/*"/>

                <xsl:for-each select="$constraints/constraint">
                    <xsl:copy copy-namespaces="no">
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>

== Concepts and Constraints provided by the <xsl:value-of select="$pluginName"/> Plugin

=== Concepts provided by the <xsl:value-of select="$pluginName"/>  plugin

        <xsl:apply-templates select="exsl:node-set($allConcepts)/concept">
            <!-- See http://www.xml.com/pub/a/2003/07/16/nodeset.html -->
            <xsl:sort select="@id" order="ascending"/>
        </xsl:apply-templates>

        <xsl:apply-templates select="exsl:node-set($allConstraints)/constraint">
            <!-- See http://www.xml.com/pub/a/2003/07/16/nodeset.html -->
            <xsl:sort select="@id" order="ascending"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="concept | constraint">
        <xsl:variable name="typeName">
            <xsl:choose>
                <xsl:when test="local-name(.) = 'constraint'">Constraint</xsl:when>
                <xsl:when test="local-name(.) = 'concept'">Concept</xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Element
                        <xsl:value-of select="local-name(.)"/> not supported.
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

<xsl:value-of select="$newline"/>
<xsl:value-of select="description"/>
<xsl:value-of select="$newline"/>
[source,cypher,indent=0]
----
<xsl:value-of select="cypher"/>
----

        <xsl:if test="requiresConcept">
Required concepts:
<xsl:for-each select="requiresConcept">
* &lt;&lt;<xsl:value-of select="@refId"/>&gt;&gt;<xsl:value-of select="$newline"/>
</xsl:for-each>
        </xsl:if>

    </xsl:template>

</xsl:stylesheet>
