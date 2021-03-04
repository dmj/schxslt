<xsl:transform version="2.0" exclude-result-prefixes="#all"
               xmlns:sch="http://purl.oclc.org/dsdl/schematron"
               xmlns:xs="http://www.w3.org/2001/XMLSchema"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:schxslt="https://doi.org/10.5281/zenodo.1495494">

  <!-- Entry for recursive inclusion -->
  <xsl:template match="sch:schema">
    <xsl:call-template name="schxslt:include">
      <xsl:with-param name="schematron" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- Recursive inclusion -->
  <xsl:template name="schxslt:include">
    <xsl:param name="schematron" as="element(sch:schema)" required="yes"/>
    <xsl:variable name="result" as="element(sch:schema)">
      <xsl:apply-templates select="$schematron" mode="schxslt:include"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$result//sch:include or $result//sch:extends[@href]">
        <xsl:call-template name="schxslt:include">
          <xsl:with-param name="schematron" select="$result" as="element(sch:schema)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$result"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sch:schema" mode="schxslt:include">
    <xsl:copy>
      <xsl:sequence select="@*"/>
      <xsl:apply-templates mode="schxslt:include"/>
    </xsl:copy>
  </xsl:template>

  <!-- Copy all other elements -->
  <xsl:template match="node() | @*" mode="schxslt:include">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <!-- Replace with contents of external definition -->
  <xsl:template match="sch:extends[@href]" mode="schxslt:include">
    <xsl:variable name="extends" select="document(@href)"/>
    <xsl:variable name="element" select="if ($extends instance of element()) then $extends else $extends/*"/>
    <xsl:if test="(local-name($element) eq local-name(..)) and (namespace-uri($element) eq 'http://purl.oclc.org/dsdl/schematron')">
      <xsl:sequence select="$element/*"/>
    </xsl:if>
  </xsl:template>

  <!-- Replace with external definition -->
  <xsl:template match="sch:include" mode="schxslt:include">
    <xsl:variable name="include" select="document(@href)"/>
    <xsl:sequence select="if ($include instance of element()) then $include else $include/*"/>
  </xsl:template>

</xsl:transform>
