<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:tns="http://schema.jqassistant.org/report/v2.0">
    <xsl:output method="html" version="1.0" encoding="iso-8859-1"
                indent="yes"/>
    <xsl:template name="content">
        <script type="text/javascript">
            function toggle(id){
              if(id.length != 0) {
                var display = document.getElementById(id).style.display;
                if(display == "table-row") {
                  document.getElementById(id).style.display = "none";
                } else {
                  document.getElementById(id).style.display = "table-row";
                }
              }
            }

            function hideAll() {
              var rows = document.getElementsByName('resultRow');
              for (var i = 0; i &lt; rows.length; ++i){
                rows[i].style.display = 'none';
              }
            }
        </script>
        <style type="text/css" onLoad="hideAll()">
            body {
            font-family:'Open Sans', sans-serif;
            line-height:1.5;
            color:#3d3a37;
            }

            a {
            color:#74bc00;
            }

            h6 {
            color:#747270;
            font-weight:normal;
            }

            table {
            width:90%;
            border-collapse:collapse;
            background-color:#e3e3e2;
            }

            table th {
            background-color:#acaba9;
            color:#fff;
            }

            table tr td, th {
            border-style:solid;
            border-width:1px;
            border-color:#fff;
            padding:5px;
            }

            table tr th {
            text-align:left;
            }

            #footer {
            color:#747270;
            }

            .right {
            text-align:right;
            }

            .nameWithResult {
            cursor:pointer;
            text-decoration:underline;
            }

            .result {
            margin:0 5px 20px 5px;
            color:#3d3a37;
            }

            .success {
            background-color:green;
            color:#fff;
            }

            .success * .ruleName:after {
            content:" \2714";
            }

            .failure {
            background-color:crimson;
            color:#fff;
            }

            .failure * .ruleName:after {
            content:" \2718";
            }

            .warning {
            background-color:orange;
            color:#fff;
            }

            .warning * .ruleName:after {
            content:" \0021";
            }
        </style>
        <h1>jQAssistant Report</h1>
        <div>
            <h3>Groups</h3>
            <table>
                <tr>
                    <th style="width:5%;">#</th>
                    <th style="width:80%;">Group Name</th>
                    <th style="width:15%;">Date</th>
                </tr>
                <xsl:apply-templates select="//tns:group"/>
            </table>
        </div>
        <div>
            <h3>Constraints</h3>
            <h6>
                <ul>
                    <li>Move the mouse over a constraint to view a description.</li>
                    <li>Click on a failed constraint to open a details view.</li>
                </ul>
            </h6>
            <table>
                <tr>
                    <th style="width:5%;">#</th>
                    <th style="width:50%;">Constraint Name</th>
                    <th style="width:15%;">Count</th>
                    <th style="width:15%;">Severity</th>
                    <th style="width:15%;">Duration (in ms)</th>
                </tr>
                <xsl:apply-templates select="//tns:constraint[tns:status='failure']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:constraint[tns:status='warning']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:constraint[not(tns:status='failure' or tns:status='warning')]">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
            </table>
        </div>
        <div>
            <h3>Concepts</h3>
            <h6>
                <ul>
                    <li>Move the mouse over a concept to view a description.</li>
                    <li>Click on a concept to open a details view.</li>
                </ul>
            </h6>
            <table>
                <tr>
                    <th style="width:5%;">#</th>
                    <th style="width:65%;">Concept Name</th>
                    <th style="width:10%;">Count</th>
                    <th style="width:10%;">Severity</th>
                    <th style="width:10%;">Duration (in ms)</th>
                </tr>
                <xsl:apply-templates select="//tns:concept[tns:status='failure']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:concept[tns:status='warning']">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="//tns:concept[not(tns:status='failure' or tns:status='warning')]">
                    <xsl:sort select="tns:severity/@level"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
            </table>
        </div>
    </xsl:template>

    <!-- ANALYSIS GROUP -->
    <xsl:template match="tns:group">
        <tr>
            <td>
                <xsl:value-of select="position()"/>
            </td>
            <td>
                <xsl:value-of select="@id"/>
            </td>
            <td>
                <xsl:value-of select="@date"/>
            </td>
        </tr>
    </xsl:template>

    <!-- CONSTRAINT/CONCEPT TABLE -->
    <xsl:template match="tns:constraint | tns:concept">
        <xsl:variable name="resultId" select="generate-id(tns:result)"/>
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="tns:status='failure'">failure</xsl:when>
                    <xsl:when test="tns:status='warning'">warning</xsl:when>
                    <xsl:when test="tns:status='success'">success</xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>

            <td>
                <xsl:value-of select="position()"/>
            </td>
            <td>
                <span class="ruleName" title="{tns:description/text()}"
                      onclick="javascript:toggle('{$resultId}');">
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="tns:result">ruleName nameWithResult</xsl:when>
                            <xsl:otherwise>ruleName</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="@id"/>
                </span>
            </td>
            <td class="right">
                <xsl:value-of select="count(tns:result/tns:rows/tns:row)"/>
            </td>
            <td class="right">
                <xsl:value-of select="tns:severity/text()"/>
            </td>
            <td class="right">
                <xsl:value-of select="tns:duration/text()"/>
            </td>
        </tr>
        <xsl:if test="tns:result">
            <tr id="{$resultId}" style="display:none;" name="resultRow">
                <td colspan="5">
                    <xsl:apply-templates select="tns:result"/>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- RESULT PART -->
    <xsl:template match="tns:result">
        <div class="result">
            <p>
                <xsl:value-of select="../tns:description/text()"/>
            </p>
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

</xsl:stylesheet>
