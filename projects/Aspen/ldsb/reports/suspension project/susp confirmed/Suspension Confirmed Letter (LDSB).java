<?xml version="1.0" encoding="UTF-8"?>
<!-- Created with Jaspersoft Studio version 6.20.6.final using JasperReports Library version 6.20.6-5c96b6aa8a39ac1dc6b6bea4b81168e16dd39231  -->
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd" name="SuspensionConfirmed" pageWidth="612" pageHeight="792" columnWidth="468" leftMargin="72" rightMargin="72" topMargin="72" bottomMargin="72" uuid="f27fa35f-69ce-4a9f-8f78-03c2e0757d74">
	<property name="ireport.scriptlethandling" value="0"/>
	<property name="ireport.encoding" value="UTF-8"/>
	<property name="ireport.zoom" value="1.6105100000000008"/>
	<property name="ireport.x" value="0"/>
	<property name="ireport.y" value="24"/>
	<property name="com.jaspersoft.studio.unit." value="pixel"/>
	<property name="com.jaspersoft.studio.unit.pageHeight" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.pageWidth" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.topMargin" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.bottomMargin" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.leftMargin" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.rightMargin" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.columnWidth" value="pixel"/>
	<property name="com.jaspersoft.studio.unit.columnSpacing" value="pixel"/>
	<import value="net.sf.jasperreports.engine.*"/>
	<import value="java.util.*"/>
	<import value="net.sf.jasperreports.engine.data.*"/>
	<import value="com.x2dev.utils.StringUtils"/>
	<parameter name="school" class="com.x2dev.sis.model.beans.SisSchool"/>
	<parameter name="longDateFormat" class="java.text.DateFormat"/>
	<parameter name="shortDateFormat" class="java.text.DateFormat"/>
	<parameter name="organization" class="com.follett.fsc.core.k12.beans.Organization"/>
	<parameter name="schoolContext" class="java.lang.Boolean"/>
	<parameter name="schoolLocale" class="org.apache.struts.util.MessageResources"/>
	<parameter name="prefix" class="java.lang.String"/>
	<parameter name="timeFormat" class="java.text.DateFormat"/>
	<parameter name="age18" class="java.lang.Boolean"/>
	<parameter name="dateOfLetterParam" class="com.x2dev.utils.types.PlainDate"/>
	<parameter name="vicePrincipal" class="java.lang.String"/>
	<parameter name="logo" class="java.lang.String"/>
	<parameter name="superintendent" class="java.lang.String"/>
	<parameter name="director" class="java.lang.String"/>
	<parameter name="directorCode" class="java.lang.String"/>
	<parameter name="superintendentTitle" class="java.lang.String"/>
	<parameter name="LocalizedLocationsMap" class="java.util.Map"/>
	<field name="action" class="com.x2dev.sis.model.beans.ConductAction"/>
	<field name="student" class="com.follett.fsc.core.k12.beans.Student"/>
	<field name="incident" class="com.x2dev.sis.model.beans.ConductIncident"/>
	<field name="person" class="com.follett.fsc.core.k12.beans.Person"/>
	<field name="age" class="java.lang.Integer"/>
	<field name="mailingAddress" class="com.follett.fsc.core.k12.beans.Address"/>
	<field name="returnDate" class="com.x2dev.utils.types.PlainDate"/>
	<field name="contactPerson" class="com.follett.fsc.core.k12.beans.Person"/>
	<field name="principal" class="com.follett.fsc.core.k12.beans.Person"/>
	<field name="primaryInfractionDesc" class="java.lang.String"/>
	<variable name="today" class="java.lang.String" resetType="None">
		<variableExpression><![CDATA[$P{shortDateFormat}.format(new java.util.Date(System.currentTimeMillis()))]]></variableExpression>
	</variable>
	<variable name="dateOfLetter" class="java.lang.String" resetType="None">
		<variableExpression><![CDATA[$P{shortDateFormat}.format($P{dateOfLetterParam})]]></variableExpression>
	</variable>
	<variable name="actionDate" class="java.lang.String">
		<variableExpression><![CDATA[$P{shortDateFormat}.format($F{action}.getActionStartDate())]]></variableExpression>
	</variable>
	<variable name="sonDaughter" class="java.lang.String">
		<variableExpression><![CDATA["F".equalsIgnoreCase($F{action}.getStudent().getPerson().getGenderCode()) ? "daughter" : "son"]]></variableExpression>
	</variable>
	<variable name="heShe" class="java.lang.String">
		<variableExpression><![CDATA["F".equalsIgnoreCase($F{action}.getStudent().getPerson().getGenderCode()) ? "she" : "he"]]></variableExpression>
	</variable>
	<variable name="hisHer" class="java.lang.String">
		<variableExpression><![CDATA["F".equalsIgnoreCase($F{action}.getStudent().getPerson().getGenderCode()) ? "her" : "his"]]></variableExpression>
	</variable>
	<variable name="numberOfDays" class="java.lang.String">
		<variableExpression><![CDATA[$F{action}.getActionPenaltyTime().intValue() + " day" + ($F{action}.getActionPenaltyTime().doubleValue() > 1 ? "s" : "")]]></variableExpression>
	</variable>
	<variable name="incidentDate" class="java.lang.String">
		<variableExpression><![CDATA[$P{shortDateFormat}.format($F{action}.getIncident().getIncidentDate())]]></variableExpression>
	</variable>
	<variable name="inSchool" class="java.lang.Boolean">
		<variableExpression><![CDATA[new Boolean($F{action}.getActionCode().toUpperCase().indexOf("OUT") == -1)]]></variableExpression>
	</variable>
	<variable name="parentGuardian" class="java.lang.String" resetType="None">
		<variableExpression><![CDATA[com.x2dev.utils.StringUtils.isEmpty($F{action}.getStudent().getFieldC001()) ? "Parent/Guardian" : $F{action}.getStudent().getFieldC001()]]></variableExpression>
	</variable>
	<variable name="schoolAddress" class="com.x2dev.sis.model.beans.SisAddress" resetType="Group" resetGroup="school">
		<variableExpression><![CDATA[$P{school}.getAddress()]]></variableExpression>
		<initialValueExpression><![CDATA[$P{school}.getAddress()]]></initialValueExpression>
	</variable>
	<variable name="fistName" class="java.lang.String" resetType="Group" resetGroup="student">
		<variableExpression><![CDATA[$F{person}.getFieldC022()]]></variableExpression>
		<initialValueExpression><![CDATA[$F{person}.getFieldC022()]]></initialValueExpression>
	</variable>
	<variable name="lastName" class="java.lang.String" resetType="Group" resetGroup="student">
		<variableExpression><![CDATA[$F{person}.getFieldC001()]]></variableExpression>
		<initialValueExpression><![CDATA[$F{person}.getFieldC001()]]></initialValueExpression>
	</variable>
	<variable name="fistName_contact" class="java.lang.String" resetType="Group" resetGroup="student">
		<variableExpression><![CDATA[$F{contactPerson}.getFirstName()]]></variableExpression>
		<initialValueExpression><![CDATA[$F{contactPerson}.getFirstName()]]></initialValueExpression>
	</variable>
	<variable name="lastName_contact" class="java.lang.String" resetType="Group" resetGroup="student">
		<variableExpression><![CDATA[$F{contactPerson}.getLastName()]]></variableExpression>
		<initialValueExpression><![CDATA[$F{contactPerson}.getLastName()]]></initialValueExpression>
	</variable>
	<variable name="localizeNumberFormat" class="java.text.NumberFormat">
		<variableExpression><![CDATA[$P{REPORT_FORMAT_FACTORY}.createNumberFormat( "#,##0.0#", $P{REPORT_LOCALE})]]></variableExpression>
	</variable>
	<group name="school" isReprintHeaderOnEachPage="true">
		<groupExpression><![CDATA[$P{school}]]></groupExpression>
		<groupHeader>
			<band height="70" splitType="Stretch">
				<property name="local_mesure_unitheight" value="pixel"/>
				<property name="com.jaspersoft.studio.unit.height" value="px"/>
				<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
					<reportElement key="textField" positionType="Float" mode="Transparent" x="0" y="57" width="88" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="8073ef8d-28db-4b85-b3ac-647e822a04c1">
						<property name="local_mesure_unitheight" value="pixel"/>
						<property name="com.jaspersoft.studio.unit.height" value="px"/>
					</reportElement>
					<box>
						<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<bottomPen lineWidth="0.0" lineColor="#000000"/>
						<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					</box>
					<textElement verticalAlignment="Top" rotation="None">
						<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
						<paragraph lineSpacing="Single"/>
					</textElement>
					<textFieldExpression><![CDATA[$V{dateOfLetter}]]></textFieldExpression>
				</textField>
				<image onErrorType="Icon">
					<reportElement stretchType="RelativeToBandHeight" x="0" y="0" width="125" height="55" uuid="e9a80957-91ae-4732-9bdf-6f9555567ff7">
						<property name="local_mesure_unitwidth" value="pixel"/>
						<property name="com.jaspersoft.studio.unit.width" value="px"/>
						<property name="local_mesure_unitheight" value="pixel"/>
						<property name="com.jaspersoft.studio.unit.height" value="px"/>
						<property name="local_mesure_unitx" value="pixel"/>
						<property name="com.jaspersoft.studio.unit.x" value="px"/>
						<printWhenExpression><![CDATA[$P{logo}!= ""]]></printWhenExpression>
					</reportElement>
					<imageExpression><![CDATA[new ByteArrayInputStream((byte[]) Base64.getDecoder().decode($P{logo}.getBytes()))]]></imageExpression>
				</image>
				<textField pattern="" isBlankWhenNull="false">
					<reportElement key="textField" mode="Transparent" x="137" y="24" width="328" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="b63ec417-6d77-447d-9c8f-6a608b6b91da">
						<property name="local_mesure_unitheight" value="pixel"/>
						<property name="com.jaspersoft.studio.unit.height" value="px"/>
					</reportElement>
					<box>
						<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<bottomPen lineWidth="0.0" lineColor="#000000"/>
						<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					</box>
					<textElement textAlignment="Right" verticalAlignment="Top" rotation="None">
						<font fontName="SansSerif" size="10" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
						<paragraph lineSpacing="Single"/>
					</textElement>
					<textFieldExpression><![CDATA[$P{school}.getName()]]></textFieldExpression>
				</textField>
				<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="true">
					<reportElement key="textField" mode="Transparent" x="170" y="37" width="295" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="f99ba794-3de4-4721-9418-b1ed70034726">
						<property name="com.jaspersoft.studio.unit.height" value="px"/>
					</reportElement>
					<box>
						<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<bottomPen lineWidth="0.0" lineColor="#000000"/>
						<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					</box>
					<textElement textAlignment="Right" verticalAlignment="Top" rotation="None">
						<font fontName="SansSerif" size="10" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
						<paragraph lineSpacing="Single"/>
					</textElement>
					<textFieldExpression><![CDATA[(!StringUtils.isBlank($V{schoolAddress}.getAddressLine01())
 ? $V{schoolAddress}.getAddressLine01()
 : "")
+ (!StringUtils.isBlank($V{schoolAddress}.getAddressLine02())
   ? ("\n" + $V{schoolAddress}.getAddressLine02())
   : "")
+ (!StringUtils.isBlank($V{schoolAddress}.getAddressLine03())
   ? ("\n" + $V{schoolAddress}.getAddressLine03())
   : "")]]></textFieldExpression>
				</textField>
				<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
					<reportElement key="textField" positionType="Float" mode="Transparent" x="170" y="50" width="295" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="a189a8f4-e740-45a3-b466-469f8544a986">
						<property name="com.jaspersoft.studio.unit.height" value="px"/>
					</reportElement>
					<box>
						<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<bottomPen lineWidth="0.0" lineColor="#000000"/>
						<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					</box>
					<textElement textAlignment="Right" verticalAlignment="Top" rotation="None">
						<font fontName="SansSerif" size="10" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
						<paragraph lineSpacing="Single"/>
					</textElement>
					<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.report.phone") + " "
+ ( $V{schoolAddress} != null
  ? ($V{schoolAddress}.getPhone01() != null
    ? $V{schoolAddress}.getPhone01()
    : "")
  : ""
  )]]></textFieldExpression>
				</textField>
				<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
					<reportElement key="textField" mode="Transparent" x="70" y="0" width="420" height="20" forecolor="#000000" backcolor="#FFFFFF" uuid="a4aa66c0-8bac-4d05-90aa-9b60dcdfecb3">
						<property name="local_mesure_unitwidth" value="pixel"/>
						<property name="com.jaspersoft.studio.unit.width" value="px"/>
					</reportElement>
					<box>
						<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
						<bottomPen lineWidth="0.0" lineColor="#000000"/>
						<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					</box>
					<textElement textAlignment="Center" verticalAlignment="Top" rotation="None">
						<font fontName="Arial" size="14" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
						<paragraph lineSpacing="Single"/>
					</textElement>
					<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.report.title")]]></textFieldExpression>
				</textField>
			</band>
		</groupHeader>
		<groupFooter>
			<band/>
		</groupFooter>
	</group>
	<group name="student">
		<groupExpression><![CDATA[$F{student}]]></groupExpression>
		<groupHeader>
			<band/>
		</groupHeader>
		<groupFooter>
			<band/>
		</groupFooter>
	</group>
	<background>
		<band splitType="Stretch"/>
	</background>
	<title>
		<band splitType="Stretch"/>
	</title>
	<columnHeader>
		<band splitType="Stretch"/>
	</columnHeader>
	<detail>
		<band height="382" splitType="Stretch">
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
				<reportElement key="1" positionType="Float" mode="Transparent" x="0" y="140" width="468" height="18" forecolor="#000000" backcolor="#FFFFFF" uuid="0e314288-aecb-4a40-ad30-eff81c474a31">
					<property name="local_mesure_unity" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[($P{age18}.booleanValue() == false ?  ($P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph1.1") + " " + $V{fistName} + " " + $V{lastName} + " "
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph1.2"))  : 
($P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph1.1.18plus"))) + ". " + "\n"]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="#,##0.0#" isBlankWhenNull="false">
				<reportElement key="2" positionType="Float" mode="Transparent" x="0" y="158" width="468" height="30" forecolor="#000000" backcolor="#FFFFFF" uuid="1361c12c-c3e8-4a29-9b41-514059974b43">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph2.1") + " " + $F{primaryInfractionDesc} + ", "
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph2.2") + " " 
+ ($F{incident}.getDescription() != null
   ? $F{incident}.getDescription()
   : "NULL")+ ". "
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph2.3") + " " + ($P{LocalizedLocationsMap}.containsKey($F{incident}.getIncidentLocation()) ? $P{LocalizedLocationsMap}.get($F{incident}.getIncidentLocation()) : $F{incident}.getIncidentLocation()) + " "
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph.on") + " " + $P{shortDateFormat}.format($F{incident}.getIncidentDate())+ " "
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph.at") + " " + $P{timeFormat}.format($F{incident}.getIncidentTime()) + ". "+ "\n"]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
				<reportElement key="3" positionType="Float" mode="Transparent" x="0" y="188" width="468" height="25" forecolor="#000000" backcolor="#FFFFFF" uuid="61723077-ab14-41f8-a8f1-7a64c6cb2837">
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph3.1") + " " + $V{fistName} + " " + $V{lastName} + " "
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph3.2")  + "  "  
+ $V{localizeNumberFormat}.format($F{action}.getActionPenaltyTime().doubleValue()) + " " +
$P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph3.3") +
"\n\n" + $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph3.4") + "\n"]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
				<reportElement key="3" positionType="Float" mode="Transparent" x="0" y="213" width="468" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="c1d023a7-9b19-43fc-97c0-486a0eca1314">
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[($P{age18}.booleanValue() == false ?  ($V{fistName} + " " + $V{lastName} + " "+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph4.1") + " " + $V{fistName} + " " + $V{lastName} + " "  
+ $P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph4.2")) : 
($P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph4.1.18plus")))]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="true">
				<reportElement key="textField" positionType="Float" mode="Transparent" x="182" y="271" width="286" height="40" forecolor="#000000" backcolor="#FFFFFF" uuid="3e9e9b30-6ff5-4977-9cb8-5e46a27c0ab1">
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<printWhenExpression><![CDATA[$P{vicePrincipal} != null]]></printWhenExpression>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Bottom" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{vicePrincipal}
+ "\n" + $P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.vice.principal")]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="true">
				<reportElement key="textField" positionType="Float" mode="Transparent" x="0" y="271" width="160" height="40" forecolor="#000000" backcolor="#FFFFFF" uuid="b4bec244-5605-4957-a7a2-6350ffde6cc2">
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Bottom" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$F{principal}.getFirstName() + " " + $F{principal}.getLastName()
+ "\n" + $P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.principal")]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="addressBlock" mode="Transparent" x="0" y="20" width="380" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="52b02066-42f5-4010-a551-26c2552ee096">
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[$P{age18}.booleanValue() == false]]></printWhenExpression>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$V{fistName_contact} + " " + $V{lastName_contact}]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="addressBlock" mode="Transparent" x="0" y="20" width="380" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="4dac9a82-b404-444f-bc68-82c422f3fa7a">
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[$P{age18}.booleanValue() == true]]></printWhenExpression>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$V{fistName} + " " + $V{lastName}]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="title" mode="Transparent" x="1" y="70" width="378" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="907792aa-b7b1-43f5-a362-5136d87676e5">
					<property name="local_mesure_unity" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.dear") + " " +
($P{age18}.booleanValue() == true ? ($V{fistName} + " " + $V{lastName}) : 
($V{fistName_contact} + " " + $V{lastName_contact})) + ","]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="0" y="100" width="20" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="48a82167-7ec0-458e-8a91-3ceabd507de8">
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.re")]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="20" y="100" width="310" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="55f32ced-4d7c-4a56-8fa2-c8ef9bc668e7">
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$V{fistName} + " " + $V{lastName}]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="0" y="112" width="192" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="16436f9b-2688-4b1e-9d0a-490488690fd1">
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.student.dob") + " " + $P{shortDateFormat}.format($F{person}.getDob())]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="330" y="100" width="138" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="0426d618-10f9-41de-8ba9-b5f4e59e74d1">
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Right" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.student.grade") + " " + $F{student}.getGradeLevel()]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="192" y="112" width="149" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="c2b62207-1545-4733-be61-6d5644b473c1">
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Center" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.student.oen") + " " + $F{student}.getStateId()]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="341" y="112" width="127" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="4c7ae6cb-d3e8-4fc9-91ae-7a383263b0c1">
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Right" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.incident.id") + " " + $F{incident}.getIncidentId()]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" mode="Transparent" x="0" y="32" width="353" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="ced91906-a952-48ee-99d7-43d4e62e6920">
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[($F{mailingAddress}.getAddressLine01() != null
 ? $F{mailingAddress}.getAddressLine01()
 : "")
+ ($F{mailingAddress}.getAddressLine02() != null
 ? "\n" + $F{mailingAddress}.getAddressLine02()
 : "")]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" positionType="Float" mode="Transparent" x="0" y="44" width="353" height="12" forecolor="#000000" backcolor="#FFFFFF" uuid="f5c7aee4-19a7-4734-85fb-d4b0f7c2733b">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[($F{mailingAddress}.getAddressLine03() != null
 ? $F{mailingAddress}.getAddressLine03()
 : "")]]></textFieldExpression>
			</textField>
			<textField pattern="" isBlankWhenNull="true">
				<reportElement key="textField" positionType="Float" mode="Transparent" x="0" y="320" width="468" height="62" forecolor="#000000" backcolor="#FFFFFF" uuid="a56b65fd-ae30-4054-bf93-c802e768ab0d">
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA["cc: \n"  +
"Attendance Counselor " + "\n" +
(($P{superintendent} != null || $P{superintendentTitle} != null) ? 
($P{superintendent} != null ? $P{superintendent}  : "") + 
    ($P{superintendentTitle} != null ?  ", " + $P{superintendentTitle} : "")
    : "") + ", " + 
	$P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.cc1")
+ "\n" + 
"Patty Gollogly, " + $P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.cc2") + "\n" + 
 $P{director} + ", " + $P{directorCode}]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false">
				<reportElement key="3" positionType="Float" mode="Transparent" x="0" y="246" width="468" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="93b55548-0d3b-4872-8de3-08ad740aef4c">
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph7")+ "\n\n"+

$P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph8")+"\n\n"+
$P{schoolLocale}.getMessage($P{prefix} + "rpt.letter.sincerely")+"\n"]]></textFieldExpression>
			</textField>
			<textField textAdjust="StretchHeight" pattern="" isBlankWhenNull="false" hyperlinkType="Reference">
				<reportElement key="3" positionType="Float" mode="Transparent" x="1" y="231" width="468" height="13" forecolor="#000000" backcolor="#FFFFFF" uuid="198aff0c-31f7-4af2-9326-eae2e0a137c4">
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="local_mesure_unitheight" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
				</reportElement>
				<box>
					<topPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<leftPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
					<bottomPen lineWidth="0.0" lineColor="#000000"/>
					<rightPen lineWidth="0.0" lineStyle="Solid" lineColor="#000000"/>
				</box>
				<textElement textAlignment="Left" verticalAlignment="Top" rotation="None">
					<font fontName="SansSerif" size="9" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false"/>
					<paragraph lineSpacing="Single"/>
				</textElement>
				<textFieldExpression><![CDATA[$P{schoolLocale}.getMessage($P{prefix} + "rpt.paragraph6")]]></textFieldExpression>
				<hyperlinkReferenceExpression><![CDATA["https://limestoneschools.sharepoint.com/:b:/s/ITSDepartment/EfO4ukqIu09Nr3e1-IcLztoBVavXPlmP81ogm9LC8I09Xw?e=6FX1uX"]]></hyperlinkReferenceExpression>
				<hyperlinkParameter name="target">
					<hyperlinkParameterExpression><![CDATA["_blank"]]></hyperlinkParameterExpression>
				</hyperlinkParameter>
			</textField>
		</band>
	</detail>
	<columnFooter>
		<band splitType="Stretch"/>
	</columnFooter>
	<pageFooter>
		<band splitType="Stretch"/>
	</pageFooter>
	<summary>
		<band height="92" splitType="Stretch">
			<printWhenExpression><![CDATA[false]]></printWhenExpression>
			<textField>
				<reportElement x="0" y="2" width="85" height="11" uuid="058c4e69-e734-4e16-97f9-333cb1227a45">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.report.title}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="2" width="85" height="11" uuid="48c88652-98ca-4f62-9cfa-5da960187a5c">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.report.phone}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="170" y="2" width="85" height="11" uuid="8f7932b5-1757-43dc-8276-4681bcbf62cf">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.dear}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="13" width="85" height="11" uuid="064f0581-967a-4722-8a36-87daecfb473f">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.student.grade}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="170" y="13" width="85" height="11" uuid="320193d0-5deb-4923-92e1-eb3974a31c51">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.student.oen}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="13" width="85" height="11" uuid="704cb1ab-3012-4278-9f9e-da14c09f11f8">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.student.dob}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="24" width="85" height="11" uuid="f79ea7ad-ad4d-480c-9d0a-767bc7798c39">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph1.1}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="35" width="85" height="11" uuid="85922302-43a1-4c1f-bcb0-f7d90a87a57e">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph2.1}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="43" width="85" height="11" uuid="6114c701-7221-4638-8c10-40903d6099b9">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph3.1}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="54" width="85" height="11" uuid="495a7125-0122-459e-b977-62365b1e88e1">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph4.1}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="79" width="85" height="11" uuid="37428631-6562-409d-ac2d-d8f80a6feb76">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.sincerely}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="79" width="85" height="11" uuid="b74e0f7e-52a3-4365-89c8-3a46a6bf1e2f">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.principal}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="170" y="79" width="85" height="11" uuid="3b14aba0-dcf4-463b-96d8-8f736d51e617">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.vice.principal}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="255" y="79" width="85" height="11" uuid="20de0cac-66f1-470a-b419-62352b2e7ecd">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.cc1}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="340" y="79" width="85" height="11" uuid="babab22a-1dec-4886-b72f-f4555ac9e3f1">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.cc2}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="425" y="79" width="85" height="11" uuid="ceaa4b2c-4a70-45ee-826a-c83b14853785">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.cc3}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="24" width="85" height="11" uuid="08061623-f554-4c0e-a9c8-089bc3932ca3">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph1.2}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="54" width="85" height="11" uuid="b9c8e873-8fbb-45c2-8d2e-6a9b83d43009">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph4.2}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="0" y="65" width="85" height="11" uuid="dc97079a-ec01-49fd-aaf2-43e3186d75cd">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph5}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="35" width="85" height="11" uuid="e20854a6-07b6-42e5-a881-d1f2f69f6dc6">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph2.2}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="255" y="2" width="85" height="11" uuid="94280a9b-ab65-4455-aac6-a46d3fa54a39">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.letter.re}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="256" y="35" width="85" height="11" uuid="c4ecd95c-e27a-4eab-b813-e18d4c8089ee">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph.on}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="340" y="35" width="85" height="11" uuid="42ceb47c-5e61-405a-8542-8cf3b6eb95e4">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph.at}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="170" y="35" width="85" height="11" uuid="ab5d1733-ac9c-43f2-b39e-800c8d269ea9">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph2.3}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="43" width="85" height="11" uuid="a300fa13-3aec-48a2-8ab0-cf2d2ada871f">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph3.2}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="256" y="13" width="83" height="11" uuid="a5a2c999-85f5-4beb-8c66-5cc724a89c2e">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.incident.id}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="85" y="65" width="85" height="11" uuid="35fb0495-2b06-461b-bff7-a9bbbff13f49">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph6}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="170" y="65" width="85" height="11" uuid="fe161fb5-8427-4640-938d-3c202a7091ee">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph7}]]></textFieldExpression>
			</textField>
			<textField>
				<reportElement x="255" y="65" width="85" height="11" uuid="cfe798c1-e00f-44e0-a28b-2a0b445d5cbc">
					<property name="local_mesure_unitx" value="pixel"/>
					<property name="local_mesure_unity" value="pixel"/>
					<property name="local_mesure_unitwidth" value="pixel"/>
					<property name="com.jaspersoft.studio.unit.width" value="px"/>
					<property name="com.jaspersoft.studio.unit.height" value="px"/>
					<property name="com.jaspersoft.studio.unit.y" value="px"/>
					<property name="com.jaspersoft.studio.unit.x" value="px"/>
					<printWhenExpression><![CDATA[false]]></printWhenExpression>
				</reportElement>
				<textElement>
					<font size="8"/>
				</textElement>
				<textFieldExpression><![CDATA[$R{rpt.paragraph8}]]></textFieldExpression>
			</textField>
		</band>
	</summary>
</jasperReport>