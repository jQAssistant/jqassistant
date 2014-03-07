<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" version="1.0" encoding="iso-8859-1" indent="yes"/>
    <xsl:template name="content">
        <script type="text/javascript">
            function toggle(id){
            if(id.length!=0){
            var display = document.getElementById(id).style.display;
            if(display=="table-row"){
            document.getElementById(id).style.display="none";
            }else{
            document.getElementById(id).style.display="table-row";
            }
            }
            }

            function hideAll(){
            var rows = document.getElementsByName('resultRow');
            for (var i = 0; i &lt; rows.length; ++i){
            rows[i].style.display='none';
            }
            }
        </script>
        <style type="text/css" onLoad="hideAll()">
            body {
            font-family: Helvetica,​arial,​freesans,​clean,​sans-serif;
            }

            table {
            width:90%;
            border-collapse: collapse;
            }

            table tr td, th {
            border-style: solid;
            border-width: 1px;
            }

            table tr th {
            text-align: left;
            }

            .right {
            text-align: right;
            }

            .nameWithResult {
            cursor:pointer;
            text-decoration:underline;
            color: blue;
            }

            .result {
            margin:0 5px 20px 5px;
            }

            .constraint_success {
            background-color:#97e568;
            }

            .constraint_success * .ruleName:after {
            content:" \2714";
            }

            .constraint_error {
            background-color:#ff8b8b;
            }

            .constraint_error * .ruleName:after {
            content:" \2718";
            }

            .concept_warn {
            background-color:#fffb65;
            }

            .concept_warn * .ruleName:after{
            content:" ?";
            }
        </style>
        <h1>jQAssistant Report</h1>
        <div>
            <h3>Groups</h3>
            <table>
                <tr>
                    <th style="width:30px;">#</th>
                    <th>Group Name</th>
                    <th style="width:150px;">Date</th>
                </tr>
                <xsl:apply-templates select="//group"/>
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
                    <th style="width:30px;">#</th>
                    <th>Constraint Name</th>
                    <th style="width:150px;">Duration (in ms)</th>
                </tr>
                <xsl:apply-templates select="//constraint">
                    <xsl:sort select="count(result)" order="descending" data-type="number"/>
                    <xsl:sort select="@id" order="ascending"/>
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
                    <th>#</th>
                    <th>Concept Name</th>
                    <th style="width:150px;">Duration (in ms)</th>
                </tr>
                <xsl:apply-templates select="//concept">
                    <xsl:sort select="count(result)" order="descending" data-type="number"/>
                    <xsl:sort select="@id" order="ascending"/>
                </xsl:apply-templates>
            </table>
        </div>
    </xsl:template>

    <!-- ANALYSIS GROUP -->
    <xsl:template match="group">
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
    <xsl:template match="constraint | concept">
        <xsl:variable name="resultId" select="generate-id(result)"/>
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="result and name()='constraint'">constraint_error</xsl:when>
                    <xsl:when test="not(result) and name()='constraint'">constraint_success</xsl:when>
                    <xsl:when test="not(result) and name()='concept'">concept_warn</xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>

            <td>
                <xsl:value-of select="position()"/>
            </td>
            <td>
                <span class="ruleName" title="{description/text()}"
                      onclick="javascript:toggle('{$resultId}');">
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="result">ruleName nameWithResult</xsl:when>
                            <xsl:otherwise>ruleName</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="@id"/>
                </span>
            </td>
            <td class="right">
                <xsl:value-of select="duration/text()"/>
            </td>
        </tr>
        <xsl:if test="result">
            <tr id="{$resultId}" style="display:table-row;" name="resultRow">
                <xsl:if test="name()='constraint'">
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="result">constraint_error</xsl:when>
                            <xsl:otherwise>constraint_success</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:if>
                <td colspan="3">
                    <xsl:apply-templates select="result"/>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- RESULT PART -->
    <xsl:template match="result">
        <div class="result">
            <p>
                <xsl:value-of select="../description/text()"/>
            </p>
            <table>
                <tr>
                    <xsl:for-each select="columns/column">
                    <th>
                            <xsl:value-of select="text()"/>
                        </th>
                    </xsl:for-each>
                </tr>
                <xsl:for-each select="rows/row">
                    <xsl:variable name="row" select="position()"/>
                    <tr>
                        <xsl:for-each select="../../columns/column">
                            <xsl:variable name="col" select="text()"/>
                            <td>
                                <xsl:value-of select="../../rows/row[$row]/column[@name=$col]"/>
                            </td>
                        </xsl:for-each>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

</xsl:stylesheet>
