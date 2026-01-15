<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tns="http://schema.jqassistant.org/report/v2.8">
    <xsl:output method="html" version="1.0" encoding="UTF-8"
                indent="yes"/>
    <xsl:template name="content">
        <script type="text/javascript" xmlns:tns="http://schema.jqassistant.org/report/v2.8">
            function getResultElement(id) {
                return document.getElementById('resultOf' + id);
            }

            function showResult(ruleId) {
                if (!ruleId) return;
                    var details = document.getElementById(ruleId);
                if (!details) return;
                details.open = true;
                details.scrollIntoView({
                    block: "start"
                    });
            }
        </script>
        <style type="text/css">
            body {
                font-family:'Open Sans', sans-serif;
                line-height:1.5;
                color:#3d3a37;
                background-color:#FFFCF0
            }

            a, a:link, a:visited, a:hover, a:focus, a:active {
                color:#000;
            }

            h6 {
                color:#747270;
                font-weight:normal;
                }

            table {
                width:90%;
                border-collapse:collapse;
                background-color:#e0ddd1;
                }

            table th {
            background-color:#aba9a1;
                color:#fff;
                }

            table tr td, th {
                border-style:solid;
                border-width:1.5px;
                border-color:#aba9a1;
                padding:5px;
            }

            table tr th {
                text-align:left;
            }

            #footer {
                color:#747270;
            }

            .report-table {
                width:90%;
                border-collapse:collapse;
                background-color:#e0ddd1;
                border-radius: 10px;
                overflow: hidden;
            }

            .header-row {
                background: #7a7974;
                font-weight: 600;
                border-bottom: 1px solid #ccc;
                background-color:#aba9a1;
                color:#fff;
            }

            .columns-grid {
                display: grid;
                grid-template-columns:  1Fr 12Fr 1Fr 1Fr 1Fr;
                padding: 8px;
            }

            .groups-grid {
                display: grid;
                grid-template-columns:  1Fr 3Fr 4Fr 1.5Fr;
                padding: 8px;
            }

            .details-content {
                margin-left: 10px;
                width:90%;
            }

            .row-separator {
                border-bottom: 1px solid #ddd;
            }

            .right {
                text-align:right;
            }

            .rule-name {
                cursor:pointer;
                text-decoration:underline;
            }

            .result {
                margin: 0px 5px 10px 5px;
                color:#3d3a37;
            }

            .success {
                background-color:green;
                color:#fff;
            }

            .failure {
                background-color:crimson;
                color:#fff;
            }

            .warning {
                background-color:orange;
                color:#fff;
            }
        </style>
        <h1 title="{/tns:jqassistant-report/tns:context/tns:build/tns:timestamp}">
            jQAssistant Report - <xsl:value-of select="/tns:jqassistant-report/tns:context/tns:build/tns:name"/>
        </h1>
        <!-- optional build properties -->
        <xsl:for-each select="/tns:jqassistant-report/tns:context/tns:build/tns:properties/tns:property">
            <div>
                <xsl:value-of select="@key"/>:
                <xsl:value-of select="text()"/>
            </div>
        </xsl:for-each>

        <div>
            <h3>Constraints</h3>
            <h6>
                <ul>
                    <li>Move the mouse over a constraint to view a description.</li>
                    <li>Click on a failed constraint to open a details view.</li>
                </ul>
            </h6>

            <section class="report-table">
                <div class="header-row columns-grid">
                    <div>#</div>
                    <div>Constraint</div>
                    <div>Status</div>
                    <div class="right">Severity</div>
                    <div class="right">Count</div>
                </div>
                <xsl:apply-templates select="//tns:constraint[tns:status='failure']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:constraint[tns:status='warning']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:constraint[not(tns:status='failure' or tns:status='warning')]">
                    <xsl:sort select="tns:verificationResult/tns:success"/>
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
            </section>
        </div>

        <div>
            <h3>Concepts</h3>
            <h6>
                <ul>
                    <li>Move the mouse over a concept to view a description.</li>
                    <li>Click on a concept to open a details view.</li>
                </ul>
            </h6>

            <section class="report-table">
                <div class="header-row columns-grid">
                    <div>#</div>
                    <div>Constraint</div>
                    <div>Status</div>
                    <div class="right">Severity</div>
                    <div class="right">Count</div>
                </div>
                <xsl:apply-templates select="//tns:concept[tns:status='failure']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:concept[tns:status='warning']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:concept[not(tns:status='failure' or tns:status='warning')]">
                    <xsl:sort select="tns:verificationResult/tns:success"/>
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
            </section>
        </div>

        <div>
            <h3>Groups</h3>
            <section class="report-table">
                <div class="header-row groups-grid">
                    <div>#</div>
                    <div>Group</div>
                    <div>Description</div>
                    <div>Date</div>
                </div>
                <xsl:apply-templates select="//tns:group"/>
            </section>
        </div>
    </xsl:template>

    <!-- ANALYSIS GROUP -->
    <xsl:template match="tns:group">

        <summary class="groups-grid">
            <div><xsl:value-of select="position()"/></div>
            <div><xsl:value-of select="@id"/></div>
            <div><xsl:value-of select="tns:description/text()"/></div>
            <div><xsl:value-of select="@date"/></div>
        </summary>
    </xsl:template>

    <!-- CONSTRAINT/CONCEPT TABLE -->
    <xsl:template match="tns:constraint | tns:concept">
        <xsl:variable name="ruleId" select="@id"/>


        <details id="{$ruleId}" class="row-separator">
            <summary>
                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="tns:status='failure'">failure columns-grid</xsl:when>
                        <xsl:when test="tns:status='warning'">warning columns-grid</xsl:when>
                        <xsl:when test="tns:status='success'">success columns-grid</xsl:when>
                    </xsl:choose>
                </xsl:attribute>

                <div>
                    <xsl:value-of select="position()"/>
                </div>

                <div>
                    <span class="rule-name" title="{tns:description/text()}">
                    <xsl:value-of select="@id"/>
                     </span>
                </div>

                <div>
                    <xsl:if test="tns:verificationResult/tns:success='false'">
                        <span title="Result verification failed">&#127783;&#160;</span>
                    </xsl:if>
                    <span title="Result evaluation according to warn-on-severity/fail-on-severity thresholds">
                        <xsl:choose>
                            <xsl:when test="tns:status='failure'">&#x2718;</xsl:when>
                            <xsl:when test="tns:status='warning'">&#x1F785;</xsl:when>
                            <xsl:when test="tns:status='success'">&#x2714;</xsl:when>
                        </xsl:choose>
                    </span>
                </div>
                <div class="right"><xsl:value-of select="tns:severity/text()"/></div>
                <div class="right"><xsl:value-of select="tns:verificationResult/tns:rowCount/text()"/></div>
            </summary>

            <div id="resultOf{$ruleId}" class="details-content" name="resultRow">
                <p>
                    <xsl:value-of select="tns:description/text()"/>
                </p>
                <p>
                     Execution Time (in ms): <xsl:value-of select="tns:duration/text()"/>
                </p>
                <xsl:choose>
                    <xsl:when test="tns:result">
                        <xsl:apply-templates select="tns:result"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <p> (no result)</p>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="tns:required-concept">
                    <table>
                        <tr>
                            <th>Required Concept</th>
                            <th>Status</th>
                        </tr>
                        <xsl:apply-templates select="tns:required-concept"/>
                    </table>
                </xsl:if>
                <xsl:if test="tns:providing-concept">
                    <table>
                        <tr>
                            <th>Providing Concept</th>
                            <th>Status</th>
                        </tr>
                        <xsl:apply-templates select="tns:providing-concept"/>
                    </table>
                </xsl:if>
            </div>
        </details>
    </xsl:template>

    <!-- RESULT PART -->
    <xsl:template match="tns:result">
        <div class="result">
            <table>
                <tr>
                    <xsl:for-each select="tns:columns/tns:column">
                        <th>
                            <xsl:value-of select="text()"/>
                        </th>
                    </xsl:for-each>
                </tr>
                <xsl:for-each select="tns:rows/tns:row">
                    <xsl:variable name="row" select="position()"/>
                    <tr>
                        <xsl:for-each select="../../tns:columns/tns:column">
                            <xsl:variable name="col" select="text()"/>
                            <td>
                                <xsl:value-of select="../../tns:rows/tns:row[$row]/tns:column[@name=$col]/tns:value"/>
                            </td>
                        </xsl:for-each>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="tns:required-concept|tns:providing-concept">
        <tr>
            <td>
                <xsl:variable name="ruleId"><xsl:value-of select="@id" /></xsl:variable>
                <span class="rule-name" onclick="javascript:showResult('{$ruleId}')">
                    <xsl:value-of select="@id"/>
                </span>
            </td>
            <td>
                <span>
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="tns:status='failure'">failure</xsl:when>
                            <xsl:when test="tns:status='warning'">warning</xsl:when>
                            <xsl:when test="tns:status='success'">success</xsl:when>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="tns:status"/>
                </span>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
