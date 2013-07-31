<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jqa-report="http://www.buschmais.com/jqassistant/core/report/schema/v1.0">
    <xsl:output method="html" version="1.0" encoding="iso-8859-1" indent="yes"/>
    <xsl:template match="/">
        <html>
            <head>
                <title>jQAssistant Report</title>
                <style type="text/css">
                	body {
                		font-family: Helvetica,​arial,​freesans,​clean,​sans-serif;
                	}
                	
                	table {
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
                	
                	.constraintName {
                		cursor:pointer;
                	}
                	                	
                	.constraintName:before {
                		content:"\21D2 ";
                	}
                	
                	.result {
                		margin:5px;
                	}
                	
                	.result:first-child {
                		text-weight:bold;
                	}
                	
                	.constraint_success {
                		background-color:#97e568;
                	}
                	
                	.constraint_error {
	                	background-color:#ff8b8b;
                	}
                </style>
            </head>
            <body>
                <h1>jQAssistant Report</h1>
                <xsl:apply-templates select="*/jqa-report:constraintGroup"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="jqa-report:constraintGroup">
        <div>
            <h2>
                Constraint Group: <xsl:value-of select="@id"/> (<xsl:value-of select="@date"/>)  
            </h2>
        </div>
        <div>
            <h3>Constraints</h3>
        </div>
        <div>
        	<table>
        		<tr>
        			<th>Constraint Name</th>
        			<th>Duration (in ms)</th>
        		</tr>
	            <xsl:apply-templates select="jqa-report:constraint"/>
            </table>
        </div>
        <div>
            <h3>Concepts</h3>
        </div>
        <div>
            <xsl:apply-templates select="jqa-report:concept"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:constraint">
		<tr class="constraint_success">
		  <xsl:attribute name="class">
			<xsl:choose>
				<xsl:when test="result">constraint_error</xsl:when>
	  			<xsl:otherwise>constraint_success</xsl:otherwise>
			</xsl:choose>
		  </xsl:attribute>
			<td>
            	<span class="constraintName" title="{jqa-report:description/text()}">
	            	<xsl:value-of select="@id"/>
            	</span>
            </td>
            <td class="right">
            	<xsl:value-of select="jqa-report:duration/text()"/>
	        </td>
		</tr>
		
		<xsl:apply-templates select="jqa-report:result" />
    </xsl:template>

    <xsl:template match="jqa-report:concept">
        <div>
            <h4>
                <xsl:value-of select="@id"/>
            </h4>
        </div>
        <div>
            <xsl:apply-templates select="jqa-report:result"/>
        </div>
    </xsl:template>

    <xsl:template match="jqa-report:result">
    	<tr class="constraint_error">
	   		<td colspan="2">
	   			<h2>Results</h2>
		    	<table class="result">
		    		<xsl:apply-templates select="jqa-report:columns"/>
		            <xsl:apply-templates select="jqa-report:rows"/>
				</table>
			</td>
		</tr>
    </xsl:template>

    <xsl:template match="jqa-report:rows">
		<xsl:for-each select="jqa-report:row">
	        <tr>
	            <xsl:apply-templates select="jqa-report:column"/>
	        </tr>
	  	</xsl:for-each>
    </xsl:template>

    <xsl:template match="jqa-report:columns">
        <tr>
			<xsl:apply-templates select="jqa-report:column"/>
        </tr>
    </xsl:template>

    <xsl:template match="jqa-report:column">
        <td>
            <xsl:value-of select="text()"/>
        </td>
    </xsl:template>

</xsl:stylesheet>
