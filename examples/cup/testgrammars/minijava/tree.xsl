<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:exsl="http://exslt.org/common"
		>
<xsl:output method="xml" version="1.0" encoding="UTF-8" />

<xsl:include href="tree-view.xsl"/>

<xsl:template match="blacklist"  mode="flatten" />

<xsl:template match="document"  mode="flatten">
      <xsl:apply-templates  mode="flatten" select="parsetree"/>
</xsl:template>

<xsl:template match="nonterminal" mode="flatten">
  <xsl:variable name="temp" select="@id" />
  <xsl:choose>
    
    <xsl:when test="count(*)=3 and contains($temp,'expr') and *[contains(@id,'expr')]"> 
      <xsl:apply-templates  mode="flatten"/>
    </xsl:when>

    <!-- collapses degenerated trees like lists, conserving the blacklist subtrees-->
    <xsl:when test="../@id = @id and count(/document/blacklist[1]/symbol[text() = $temp])=0"> 
      <xsl:apply-templates  mode="flatten"/>
    </xsl:when>

    <!-- collapses unary productions -->
    <xsl:when test="count(*)=3 and count(/document/blacklist[1]/symbol[text() = $temp])=0"> 
      <xsl:apply-templates  mode="flatten"/>
    </xsl:when>

    <xsl:otherwise>
      <xsl:element name="{@id}" >
	<xsl:attribute name="variant"><xsl:value-of select="@variant" /></xsl:attribute>
	<xsl:apply-templates mode="flatten"/>
      </xsl:element>
    </xsl:otherwise>

  </xsl:choose>
</xsl:template>

<xsl:template match="terminal" mode="flatten">
  <xsl:element name="{@id}">
    <xsl:apply-templates  mode="flatten"/>
  </xsl:element>
</xsl:template>

<xsl:template match="/">
 <xsl:variable name="flatten">
     <xsl:apply-templates  mode="flatten"/>
 </xsl:variable>
 <xsl:variable name="rendered">
    <xsl:apply-templates mode="rendered" select="exsl:node-set($flatten)"/>
 </xsl:variable>

 <!--xsl:copy-of select="$rendered" /-->
 <xsl:copy-of select="$flatten" />
</xsl:template>


</xsl:stylesheet>