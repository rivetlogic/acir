<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8"/>
	<xsl:template match="/">
		<output-one>
			<xsl:attribute name="identifier">
				<xsl:value-of select="/catalog/@id"/>
			</xsl:attribute>
		</output-one>
	</xsl:template>
</xsl:stylesheet>