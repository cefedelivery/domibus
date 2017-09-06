<?xml version="1.0" encoding="utf-8"?>



<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                xmlns:S12="http://www.w3.org/2003/05/soap-envelope"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:wsa="http://www.w3.org/2005/08/addressing"
                xmlns:ebint="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/multihop/200902/"
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
                xmlns:eb3="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/"
                xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                xmlns:ebbp="http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0"
                xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
                exclude-result-prefixes="xd xsi"
                version="1.0">
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p>
                <xd:b>Created on:</xd:b>
                Feb 5, 2012
            </xd:p>
            <xd:p>
                <xd:b>Author:</xd:b>
                Pim van der Eijk, minor changes by Christian Koch, Stefan Mueller
            </xd:p>
            <xd:p>This XSLT stylesheet is a modified version of the stylesheet found in the AS4 specification
            </xd:p>
        </xd:desc>
        <xd:param name="messageid">
            <xd:p>The messageid to use on the AS4 receipt signal message</xd:p>
        </xd:param>
        <xd:param name="timestamp">
            <xd:p>The timestamp to set on the AS4 receipt signal message</xd:p>
        </xd:param>
    </xd:doc>

    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="messageid" select="'not-set'"/>
    <xsl:param name="timestamp" select="'not-set'"/>
    <xsl:param name="nonRepudiation" select="'not-set'"/>

    <xsl:template match="S12:Envelope">
        <S12:Envelope>
            <xsl:apply-templates/>
        </S12:Envelope>
    </xsl:template>

    <xsl:template match="S12:Header">
        <S12:Header>
            <xsl:apply-templates select="eb3:Messaging"/>
        </S12:Header>
    </xsl:template>

    <xd:doc>
        <xd:desc>When generating a receipt for a signed message, the receipt will be signed as well.
            We generate an identifier for the empty SOAP Body of the AS4 receipt for the WS-Security
            module.
        </xd:desc>
    </xd:doc>
    <xsl:template match="S12:Envelope[S12:Header//ds:Signature]/S12:Body">
        <S12:Body wsu:Id="{generate-id()}"/>
    </xsl:template>

    <xd:doc>
        <xd:desc>The empty body of receipt signal receipt for an unsigned message does not need an
            identifier
        </xd:desc>
    </xd:doc>

    <xsl:template match="S12:Envelope[not(S12:Header//ds:Signature)]/S12:Body">
        <S12:Body/>
    </xsl:template>

    <xd:doc>
        <xd:desc>There are two templates for
            <xd:i>eb3:Messaging</xd:i>
            element. This first template
            is for an AS4 user message that may have been exchanged over a multi-hop network. The
            receipt for such a message has WS-Addressing headers and a routing
            parameter based on
            the user message content.
        </xd:desc>
    </xd:doc>
    <xsl:template
            match="eb3:Messaging[
        @S12:role='http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/nextmsh']">
        <xsl:variable name="mpc">
            <xsl:choose>
                <xsl:when test="descendant::eb3:UserMessage[1]/@mpc">
                    <xsl:value-of select="descendant::eb3:UserMessage[1]/@mpc"/>
                </xsl:when>
                <xsl:otherwise>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <wsa:To wsu:Id="{concat('_wsato_',generate-id())}"
                S12:role="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/nextmsh"
                S12:mustUnderstand="true"
                >
            http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/icloud
        </wsa:To>
        <wsa:Action wsu:Id="{concat('_wsaaction_',generate-id())}"
                >
            http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/oneWay.receipt
        </wsa:Action>
        <ebint:RoutingInput wsa:IsReferenceParameter="true"
                            id="{concat('_ebroutinginput_',generate-id())}" S12:mustUnderstand="true"
                            S12:role="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/nextmsh">
            <ebint:UserMessage mpc="{concat($mpc,'.receipt')}">
                <eb3:PartyInfo>
                    <eb3:From>
                        <xsl:copy-of select="descendant::eb3:UserMessage[1]//eb3:To/eb3:PartyId"/>
                        <xsl:copy-of select="descendant::eb3:UserMessage[1]//eb3:To/eb3:Role"/>
                    </eb3:From>
                    <eb3:To>
                        <xsl:copy-of select="descendant::eb3:UserMessage[1]//eb3:From/eb3:PartyId"/>
                        <xsl:copy-of select="descendant::eb3:UserMessage[1]//eb3:From/eb3:Role"/>
                    </eb3:To>
                </eb3:PartyInfo>
                <eb3:CollaborationInfo>
                    <xsl:copy-of select="descendant::eb3:UserMessage[1]//eb3:Service"/>
                    <eb3:Action>
                        <xsl:value-of
                                select="concat(descendant::eb3:UserMessage[1]//eb3:Action,'.receipt')"/>
                    </eb3:Action>
                    <xsl:copy-of select="descendant::eb3:UserMessage[1]//eb3:ConversationId"/>
                </eb3:CollaborationInfo>
            </ebint:UserMessage>
        </ebint:RoutingInput>
        <eb3:Messaging S12:mustUnderstand="true" id="{concat('_ebmessaging_',generate-id())}">
            <xsl:apply-templates select="descendant-or-self::eb3:UserMessage"/>
        </eb3:Messaging>
    </xsl:template>

    <xd:doc>
        <xd:desc>This second template for the
            <xd:i>eb3:Messaging</xd:i>
            element covers AS4
            point-to-point messages.
        </xd:desc>
    </xd:doc>
    <xsl:template
            match="eb3:Messaging[not(
        @S12:role='http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/nextmsh')]">
        <eb3:Messaging S12:mustUnderstand="true" id="{concat('_ebmessaging_',generate-id())}">
            <xsl:apply-templates select="descendant-or-self::eb3:UserMessage"/>
        </eb3:Messaging>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>The AS4 receipt is generated based on
                <xd:i>eb3:UserMessage</xd:i>
                and
                <xd:i>ds:Signature</xd:i>content.
            </xd:p>
            <xd:ul>
                <xd:li>A receipt for a signed AS4 message references the message parts using
                    <xd:i>ds:Reference</xd:i>s in the WS-Security header of that
                    message
                </xd:li>
                <xd:li>A receipt for an unsigned AS4 message references the message using the
                    <xd:i>eb3:UserMessage</xd:i>s of the AS4 message.
                </xd:li>
            </xd:ul>
        </xd:desc>
    </xd:doc>
    <xsl:template match="eb3:UserMessage">
        <xsl:if test="$messageid = 'not-set'">
            <xsl:message terminate="yes">ERROR: messageId was not set</xsl:message>
        </xsl:if>
        <xsl:if test="$timestamp = 'not-set'">
            <xsl:message terminate="yes">ERROR: timestamp was not set</xsl:message>
        </xsl:if>
        <xsl:if test="$nonRepudiation = 'not-set'">
            <xsl:message terminate="yes">ERROR: nonRepudiation flag was not set</xsl:message>
        </xsl:if>
        <eb3:SignalMessage>
            <eb3:MessageInfo>
                <eb3:Timestamp>
                    <xsl:value-of select="$timestamp"/>
                </eb3:Timestamp>
                <eb3:MessageId>
                    <xsl:value-of select="$messageid"/>
                </eb3:MessageId>
                <eb3:RefToMessageId>
                    <xsl:value-of select="descendant::eb3:MessageId"/>
                </eb3:RefToMessageId>
            </eb3:MessageInfo>
            <eb3:Receipt>
                <xsl:choose>
                    <xsl:when test="$nonRepudiation = 'true'">
                        <xsl:choose>
                            <xsl:when test="/S12:Envelope/S12:Header/wsse:Security/ds:Signature">
                                <ebbp:NonRepudiationInformation>
                                    <xsl:apply-templates
                                            select="/S12:Envelope/S12:Header//ds:Reference"/>
                                </ebbp:NonRepudiationInformation>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message terminate="yes">ERROR: Missing
                                    Security
                                    Header
                                </xsl:message>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="//eb3:UserMessage"/>
                    </xsl:otherwise>
                </xsl:choose>
            </eb3:Receipt>
        </eb3:SignalMessage>
    </xsl:template>

    <xsl:template match="ds:Reference">
        <ebbp:MessagePartNRInformation>
            <xsl:copy-of select="current()"/>
        </ebbp:MessagePartNRInformation>
    </xsl:template>
</xsl:stylesheet>
