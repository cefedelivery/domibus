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
<form method="POST" action="updatepmode" enctype="multipart/form-data">
  PMode File to upload: <input type="file" name="file"><br/>
  <br/>
  <input type="submit" value="Upload"> Press here to upload the PMode file!
</form>
</body>
</html>
