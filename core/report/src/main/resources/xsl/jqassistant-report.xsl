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
                	
                	.concept_warn  * .ruleName:after{
                		content:" [not executed]";
                	}
                </style>
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
            </head>
            <body onload="hideAll()">
                <h1>jQAssistant Report</h1>
				<div>
			    	TOC:
			    	<ul>
		                <xsl:apply-templates select="*/jqa-report:constraintGroup" mode="toc"/>
		            </ul>
		        </div>
                <xsl:apply-templates select="*/jqa-report:constraintGroup" mode="full"/>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="jqa-report:constraintGroup" mode="toc">
		<li><xsl:value-of select="@id"/>
			<ul>
				<li><a href="#{@id}_constraints">Constraints</a></li>
				<li><a href="#{@id}_concepts">Concepts</a></li>
			</ul>
		</li>
    </xsl:template>
    
	<!-- CONSTRAINT GROUP -->
    <xsl:template match="jqa-report:constraintGroup" mode="full">
        <div>
            <h2>
                Constraint Group: <xsl:value-of select="@id"/> (<xsl:value-of select="@date"/>) 
            </h2>
        </div>
        <div>
            <h3><a name="{@id}_constraints">Constraints</a></h3>
        	<table>
        		<tr>
        			<th>#</th>
        			<th>Constraint Name</th>
        			<th>Duration (in ms)</th>
        		</tr>
	            <xsl:apply-templates select="jqa-report:constraint">
	            	<xsl:sort select="count(jqa-report:result)" order="descending" data-type="number" />
            	</xsl:apply-templates>
            </table>
        </div>
        <div>
            <h3><a name="{@id}_concepts">Concepts</a></h3>
            <table>
            	<tr>
            		<th>#</th>
            		<th>Concept Name</th>
            		<th>Duration (in ms)</th>
	           	</tr>
            	<xsl:apply-templates select="jqa-report:concept">
	            	<xsl:sort select="count(jqa-report:result)" order="descending" data-type="number" />
            	</xsl:apply-templates>
            </table>
        </div>
    </xsl:template>
    
	<!-- CONSTRAINT/CONCEPT TABLE -->
    <xsl:template match="jqa-report:constraint | jqa-report:concept">
		<xsl:variable name="resultId" select="generate-id(jqa-report:result)"/>
		<tr>
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="jqa-report:result and name()='constraint'">constraint_error</xsl:when>
					<xsl:when test="not(jqa-report:result) and name()='constraint'">constraint_success</xsl:when>
					<xsl:when test="not(jqa-report:result) and name()='concept'">concept_warn</xsl:when>
		  			<xsl:otherwise></xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			
			<td style="width:30px;">
				<xsl:value-of select="position()"/>
			</td>
			<td>
            	<span class="ruleName" title="{jqa-report:description/text()}" onclick="javascript:toggle('{$resultId}');">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="jqa-report:result">ruleName nameWithResult</xsl:when>
				  			<xsl:otherwise>ruleName</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
	            	<xsl:value-of select="@id"/>
            	</span>
            </td>
            <td class="right" style="width:150px;">
            	<xsl:value-of select="jqa-report:duration/text()"/>
	        </td>
		</tr>
		<xsl:if test="jqa-report:result">
	    	<tr id="{$resultId}" style="display:table-row;" name="resultRow">
	    		<xsl:if test="name()='constraint'">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="jqa-report:result">constraint_error</xsl:when>
				  			<xsl:otherwise>constraint_success</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
				</xsl:if>
		   		<td colspan="3">
					<xsl:apply-templates select="jqa-report:result" />
				</td>
			</tr>
		</xsl:if>
    </xsl:template>
    
	<!-- RESULT PART -->
    <xsl:template match="jqa-report:result">
		<div class="result">
			<h4>Results</h4>
			<p><xsl:value-of select="../jqa-report:description/text()"/></p>			
	    	<table>
	    		<tr>
		    		<xsl:for-each select="jqa-report:columns/jqa-report:column">
		    			<th>
					    	<xsl:value-of select="text()"/>
		    			</th>
		    		</xsl:for-each>
	    		</tr>
				<xsl:for-each select="jqa-report:rows/jqa-report:row">
					<xsl:variable name="row" select="position()" />
			        <tr>
						<xsl:for-each select="../../jqa-report:columns/jqa-report:column">
							<xsl:variable name="col" select="text()" />
					        <td>
								<xsl:value-of select="../../jqa-report:rows/jqa-report:row[$row]/jqa-report:column[@name=$col]"/>
						    </td>
					  	</xsl:for-each>
			        </tr>
				</xsl:for-each>		  	
			</table>
		</div>
    </xsl:template>

</xsl:stylesheet>
