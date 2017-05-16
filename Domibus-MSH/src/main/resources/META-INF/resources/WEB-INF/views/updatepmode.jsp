<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%--
  ~ Copyright 2015 e-CODEX Project
  ~
  ~ Licensed under the EUPL, Version 1.1 or – as soon they
  ~ will be approved by the European Commission - subsequent
  ~ versions of the EUPL (the "Licence");
  ~ You may not use this work except in compliance with the
  ~ Licence.
  ~ You may obtain a copy of the Licence at:
  ~ http://ec.europa.eu/idabc/eupl5
  ~ Unless required by applicable law or agreed to in
  ~ writing, software distributed under the Licence is
  ~ distributed on an "AS IS" basis,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  ~ express or implied.
  ~ See the Licence for the specific language governing
  ~ permissions and limitations under the Licence.
  --%>

<html>
<head>
    <jsp:include page="header.jsp"/>
</head>
<body>
<form method="POST" action="uploadPmodeFile" enctype="multipart/form-data">

    <h3>PMode upload:</h3> <br/>
    PMode xml file to upload:<br/>
    <input type="file" name="pmode"><br/>

    <br/>
    <input type="submit" value="Press here to upload the PMode xml file">
    Please notice that the TrustStore will be re-loaded.
</form>
<br/>
<br/>

<form method="POST" action="uploadTruststoreFile" enctype="multipart/form-data">

    <h3>Truststore upload:</h3> <br/>
    Truststore JKS file to upload:<br/>
    <input type="file" name="truststore"><br/>

    Truststore password: <br/>
    <input type="text" name="password"><br/>
    <br/>
    <input type="submit" value="Press here to upload the truststore jks file">
</form>
</body>
</html>
