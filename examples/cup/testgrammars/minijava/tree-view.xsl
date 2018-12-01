<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" 
	      encoding="UTF-8"
	      indent="no"/>

  <xsl:variable name="apos">'</xsl:variable>

  <xsl:template match="/">
    <html>
      <head>
	<meta charset="UTF-8" />
        <title>Parse-Tree</title>
        <link type="text/css" rel="stylesheet" href="tree-view.css"/>
      </head>
      <body>
        <h3>Parse-Tree</h3>
        <xsl:apply-templates select="." mode="render"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="/" mode="render">
    <xsl:apply-templates mode="render"/>
  </xsl:template>

  <xsl:template match="*" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <br/>
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>___</span>
    <span class="element"><xsl:value-of select="local-name()"/></span>
    <xsl:text>&#160;</xsl:text>
    <br/>
    <xsl:apply-templates select="@*" mode="render"/>
    <xsl:apply-templates mode="render"/>
  </xsl:template>

  <xsl:template match="@*" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>&#160;&#160;</span>
    <span class='connector'>\___</span>

    <xsl:text>&#160;</xsl:text>
    <span class="name">
      <xsl:value-of select="local-name()"/>
    </span>
    <xsl:text> = </xsl:text>
    <span class="value">
      <xsl:call-template name="escape-ws">
        <xsl:with-param name="text" select="translate(.,' ','&#160;')"/>
      </xsl:call-template>
    </span>
    <br/>
  </xsl:template>

  <xsl:template match="text()" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <br/>
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>___</span>
    <xsl:text>&#160;</xsl:text>
    <span class="value">
      <xsl:call-template name="escape-ws">
        <xsl:with-param name="text" select="translate(.,' ','&#160;')"/>
      </xsl:call-template>
    </span>
    <br/>
  </xsl:template>

  <xsl:template match="comment()" mode="render" />
  <xsl:template match="processing-instruction()" mode="render" />


  <xsl:template name="ascii-art-hierarchy">
    <xsl:for-each select="ancestor::*">
      <xsl:choose>
        <xsl:when test="following-sibling::node()">
          <span class='connector'>&#160;&#160;</span>|<span class='connector'>&#160;&#160;</span>
          <xsl:text>&#160;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <span class='connector'>&#160;&#160;&#160;&#160;</span>
          <span class='connector'>&#160;&#160;</span>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="parent::node() and ../child::node()">
        <span class='connector'>&#160;&#160;</span>
        <xsl:text>|</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <span class='connector'>&#160;&#160;&#160;</span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- recursive template to escape linefeeds, tabs -->
  <xsl:template name="escape-ws">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text, '&#xA;')">
        <xsl:call-template name="escape-ws">
          <xsl:with-param name="text" select="substring-before($text, '&#xA;')"/>
        </xsl:call-template>
        <span class="escape">\n</span>
        <xsl:call-template name="escape-ws">
          <xsl:with-param name="text" select="substring-after($text, '&#xA;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($text, '&#x9;')">
        <xsl:value-of select="substring-before($text, '&#x9;')"/>
        <span class="escape">\t</span>
        <xsl:call-template name="escape-ws">
          <xsl:with-param name="text" select="substring-after($text, '&#x9;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
