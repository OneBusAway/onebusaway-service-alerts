<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <active-reroutes>
            <xsl:for-each select="table/tr">
                <active-reroute>
                    <routeId>
                        <xsl:value-of select="td[1]" />
                    </routeId>
                    <region>
                        <xsl:value-of select="td[2]" />
                    </region>
                    <neighborhood>
                        <xsl:value-of select="td[3]" />
                    </neighborhood>
                    <description>
                        <xsl:value-of select="td[4]/a" />
                    </description>
                    <url>
                        <xsl:value-of select="td[4]/a/@href" />
                    </url>
                    <mapUrl>
                        <xsl:value-of select="td[6]/a/@href" />
                    </mapUrl>
                </active-reroute>
            </xsl:for-each>
        </active-reroutes>
    </xsl:template>
</xsl:stylesheet>