<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tns="http://schema.jqassistant.org/report/v2.9">
    <xsl:output method="html" version="1.0" encoding="UTF-8"
                indent="yes"/>
    <xsl:template name="content">
        <script type="text/javascript" xmlns:tns="http://schema.jqassistant.org/report/v2.9">
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

            document.addEventListener("DOMContentLoaded", () => {
                document.querySelectorAll(".timeStamp").forEach(stamp => {
                    const date = new Date(stamp.textContent);
                    const options = {
                        day: "2-digit",
                        month: "2-digit",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                        hour12: false,
                        timeZoneName: "short"
                    }
                    const parts = new Intl.DateTimeFormat("en-UK", options).formatToParts(date);
                    function getValue(type) {
                        for (var i = 0; i &lt; parts.length; i++) {
                            if (parts[i].type === type){
                                return parts[i].value;
                            }
                        }
                        return "";
                    }
                    stamp.textContent =
                        `${getValue("day")}.`
                        +`${getValue("month")}.`
                        +`${getValue("year")} `
                        +`${getValue("hour")}:`
                        +`${getValue("minute")} `
                        +`(${getValue("timeZoneName")})`;
                });
            });
        </script>
        <style type="text/css">
            body {
                font-family:'Open Sans', sans-serif;
                line-height:1.5;
                color:#3d3a37;
                background-color:#FFFCF0
                overflow: auto;
                width:80%
            }

            a, a:link, a:visited, a:hover, a:focus, a:active {
                color:#000;
            }

            h1 {
                margin: 0;
            }

            h5 {
            margin: 0;
            }

            h6 {
                color:#747270;
                font-weight:normal;
                margin: 0;
                margin-left: 2%;
                }

            h3 {
                margin-bottom: 0;
            }

            ul {
                margin-top: 0;
            }

            .abstractRule {
                font-style: italic;
            }

            table {
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
                white-space: nowrap;
            }

            table tr th {
                text-align:left;
            }

            #footer {
                color:#747270;
            }

            .report-table {
                display:inline-block;
                width: max-content
                border-collapse:collapse;
                background-color:#e0ddd1;
                border-radius: 10px;
                white-space: normal;
                overflow-wrap: anywhere;
                overflow: hidden;
                margin-right:10px;

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
                grid-template-columns:  minmax(40px,2Fr) minmax(60px,2Fr) minmax(300px,50Fr) minmax(75px,2Fr) minmax(50px,2Fr);
                padding: 6px;
            }

            .groups-grid {
                display: grid;
                grid-template-columns:  minmax(40px,2Fr) minmax(175px,15Fr) minmax(200px,24.5Fr) minmax(200px,2Fr);
                padding: 6px;
            }

            .details-content {
                margin-left: 10px;
                margin-bottom:10px;
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
                background-color:#43a047; <!-- green -->
                color:#fff;
            }

            .failure {
                background-color:#ce413c; <!-- red -->
                color:#fff;
            }

            .warning {
                background-color:#e3ad24; <!-- orange -->
                color:#fff;
            }

            .skipped {
                background-color:#bfbdb4; <!-- grey -->
            }
        </style>
        <h1>
            jQAssistant Report - <xsl:value-of select="/tns:jqassistant-report/tns:context/tns:build/tns:name"/>
        </h1>
        <h5>
            Time Stamp:
            <span class="timeStamp">
                 <xsl:value-of select="/tns:jqassistant-report/tns:context/tns:build/tns:timestamp"/>
            </span>
        </h5>
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
                    <div>Status</div>
                    <div>Constraint</div>
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
                    <li>Abstract concepts are shown in italics.</li>
                </ul>
            </h6>

            <section class="report-table">
                <div class="header-row columns-grid">
                    <div>#</div>
                    <div>Status</div>
                    <div>Constraint</div>
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
                    <div class="right">Date</div>
                </div>
                <xsl:apply-templates select="//tns:group"/>
            </section>
        </div>
    </xsl:template>

    <!-- ANALYSIS GROUP -->
    <xsl:template match="tns:group">
        <xsl:variable name="groupId" select="@id"/>
        <summary class="groups-grid">
            <div>
                <xsl:value-of select="position()"/>
            </div>
            <div>
                <xsl:choose>
                    <xsl:when test="tns:description/text() or tns:overrides-group/@id">
                        <span class="rule-name" title="{tns:description/text()}"
                              onclick="javascript:showResult('{$groupId}');">
                            <xsl:value-of select="@id"/>
                        </span>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@id"/>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
            <div>
                <xsl:if test="tns:description">
                    <xsl:value-of select="tns:description/text()"/>
                </xsl:if>
                <xsl:if test="tns:overrides-group">
                    <xsl:for-each select="tns:overrides-group[@id]">
                        <xsl:if test="position() = 1"> Overrides: </xsl:if>
                        <xsl:value-of select="@id"/>
                        <xsl:if test="position() != last()"> , </xsl:if>
                    </xsl:for-each>
                </xsl:if>
            </div>
            <div class="right">
                <xsl:value-of select="@date"/>
            </div>
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
                        <xsl:when test="tns:status='skipped'">skipped columns-grid</xsl:when>
                    </xsl:choose>
                </xsl:attribute>

                <div>
                    <xsl:value-of select="position()"/>
                </div>

                <div>
                    <xsl:choose>
                        <xsl:when test="tns:status='skipped'"/>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="tns:verificationResult/tns:success='false'">
                                    <xsl:choose>
                                        <xsl:when test="self::tns:constraint">
                                            <span title="No Violations Found">&#127783;&#160;</span>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <span title="No Matches Found">&#127783;&#160;</span>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when test="self::tns:constraint">
                                            <span title="Violations Found">&#9728;&#160;</span>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <span title="Matches Found">&#9728;&#160;</span>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="tns:status='failure'">
                            <span title="Failure (Result evaluation according to warn-on-severity/fail-on-severity thresholds)">&#x2718;</span>
                        </xsl:when>
                        <xsl:when test="tns:status='warning'">
                            <span title="Warning (Result evaluation according to warn-on-severity/fail-on-severity thresholds)">&#x1F785;</span>
                        </xsl:when>
                        <xsl:when test="tns:status='success'">
                            <span title="Success (Result evaluation according to warn-on-severity/fail-on-severity thresholds)">&#x2714;</span>
                        </xsl:when>

                    </xsl:choose>
                </div>

                <div>
                    <span>
                        <xsl:attribute name="title">
                            <xsl:value-of select="tns:description/text()"/>
                            <xsl:choose>
                                <xsl:when test="@typeAbstract = 'true'">
                                    <xsl:text>&lt;abstract&gt;</xsl:text>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:attribute>

                        <xsl:attribute name="class">
                            <xsl:choose>
                                <xsl:when test="@typeAbstract = 'true'">
                                    <xsl:text>abstractRule rule-name</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>rule-name</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:value-of select="@id"/>
                    </span>
                </div>

                <div class="right"><xsl:value-of select="tns:severity/text()"/></div>
                <div class="right"><xsl:value-of select="tns:verificationResult/tns:rowCount/text()"/></div>
            </summary>

            <div id="resultOf{$ruleId}" class="details-content" name="resultRow">
                <p>
                    <xsl:value-of select="tns:description/text()"/>
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
                <xsl:if test="tns:overrides-concept">
                    <xsl:for-each select="tns:overrides-concept[@id]">
                        <xsl:if test="position() = 1"> Overrides: </xsl:if>
                        <xsl:value-of select="@id"/>
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="tns:overrides-constraint">
                    <xsl:for-each select="tns:overrides-constraint[@id]">
                        <xsl:if test="position() = 1"> Overrides: </xsl:if>
                        <xsl:value-of select="@id"/>
                        <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
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
