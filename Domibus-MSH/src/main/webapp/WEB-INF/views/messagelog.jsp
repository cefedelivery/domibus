<%@page session="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <!-- basic filter URL -->
    <c:url value="messagelog" var="filteredmessagelog">
        <c:if test="${not empty param.messageId}"><c:param name="messageId" value="${param.messageId}"/></c:if>
        <c:if test="${not empty param.messageStatus}"><c:param name="messageStatus"
                                                               value="${param.messageStatus}"/></c:if>
        <c:if test="${not empty param.notificationStatus}"><c:param name="notificationStatus"
                                                                    value="${param.notificationStatus}"/></c:if>
        <c:if test="${not empty param.mshRole}"><c:param name="mshRole" value="${param.mshrole}"/></c:if>
        <c:if test="${not empty param.messageType}"><c:param name="messageType" value="${param.messageType}"/></c:if>
        <c:if test="${not empty param.receivedFrom}"><c:param name="receivedFrom" value="${param.receivedFrom}"/></c:if>
        <c:if test="${not empty param.receivedTo}"><c:param name="receivedTo" value="${param.receivedTo}"/></c:if>
    </c:url>
    <!-- paging URLs -->
    <c:url value="${filteredmessagelog}" var="toPage">
        <c:param name="size" value="${size}"/>
        <c:if test="${not empty column}">
            <c:param name="orderby" value="${column}"/>
            <c:param name="asc" value="${asc}"/>
        </c:if>
    </c:url>
    <!-- table resize URL -->
    <c:url value="${filteredmessagelog}" var="resizeTable">
        <c:param name="page" value="${page}"/>
        <c:if test="${not empty column}">
            <c:param name="orderby" value="${column}"/>
            <c:param name="asc" value="${asc}"/>
        </c:if>
    </c:url>
    <!-- order URLs -->
    <c:url value="${filteredmessagelog}" var="order">
        <c:param name="page" value="${page}"/>
        <c:param name="size" value="${size}"/>
    </c:url>
</head>
<body>
<h1>${title}</h1>

<div id="filtermask">
    <form action="messagelog" method="get">
        <h3>Filter:</h3>
        MessageId: <input name="messageId" type="text" size="30" maxlength="30">
        MessageStatus: <select name="messageStatus" size="1">
        <option/>
        <c:forEach var="ms" items="${messagestatusvalues}">
            <option>${ms}</option>
        </c:forEach></select>
        NotificationStatus: <select name="notificationStatus" size="1">
        <option/>
        <c:forEach var="ns" items="${notificationstatusvalues}">
            <option>${ns}</option>
        </c:forEach></select>
        MshRole: <select name="mshRole" size="1">
        <option/>
        <c:forEach var="msh" items="${mshrolevalues}">
            <option>${msh}</option>
        </c:forEach></select>
        MessageType: <select name="messageType" size="1">
        <option/>
        <c:forEach var="mt" items="${messagetypevalues}">
            <option>${mt}</option>
        </c:forEach></select>
        <br/>
        <nobr><h4>Received:</h4> From: <input name="receivedFrom" type="text" size="20" maxlength="30"> To: <input
                name="receivedTo" type="text" size="20" maxlength="30"></nobr>
        <input name="size" type="hidden" value="${size}">
        <input type="submit" value="Search">
    </form>
</div>
<!-- row selection -->
<div>
    Row:
    <select name="rows" size="1" onchange="location.href = this.value;">
        <option value="<c:url value="${resizeTable}"><c:param name="size" value="10"/></c:url>" <c:if
                test="${size == 10}"> selected</c:if>>10
        </option>
        <option value="<c:url value="${resizeTable}"><c:param name="size" value="15"/></c:url>" <c:if
                test="${size == 15}"> selected</c:if>>15
        </option>
        <option value="<c:url value="${resizeTable}"><c:param name="size" value="20"/></c:url>" <c:if
                test="${size == 20}"> selected</c:if>>20
        </option>
        <option value="<c:url value="${resizeTable}"><c:param name="size" value="25"/></c:url>" <c:if
                test="${size == 25}"> selected</c:if>>25
        </option>
        <option value="<c:url value="${resizeTable}"><c:param name="size" value="50"/></c:url>" <c:if
                test="${size == 50}"> selected</c:if>>50
        </option>
    </select>
</div>
<!-- table content -->
<table>
    <tr>
        <c:url value="${order}" var="messageId"><c:param name="orderby" value="messageId"/></c:url>
        <th>MessageId <a href="<c:url value="${messageId}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \
            <a href="<c:url value="${messageId}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="messageStatus"><c:param name="orderby" value="messageStatus"/></c:url>
        <th>MessageStatus <a href="<c:url value="${messageStatus}"><c:param name="asc" value = "true"/></c:url>">
            &#9650;</a> \ <a href="<c:url value="${messageStatus}"><c:param name="asc" value = "false"/></c:url>">
            &#9660;</a></th>
        <c:url value="${order}" var="notificationStatus"><c:param name="orderby" value="notificationStatus"/></c:url>
        <th>NotificationStatus <a
                href="<c:url value="${notificationStatus}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \
            <a href="<c:url value="${notificationStatus}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a>
        </th>
        <c:url value="${order}" var="mshRole"><c:param name="orderby" value="mshRole"/></c:url>
        <th>MshRole <a href="<c:url value="${mshRole}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \ <a
                href="<c:url value="${mshRole}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="messageType"><c:param name="orderby" value="messageType"/></c:url>
        <th>MessageType <a href="<c:url value="${messageType}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a>
            \ <a href="<c:url value="${messageType}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="deleted"><c:param name="orderby" value="deleted"/></c:url>
        <th>Deleted <a href="<c:url value="${deleted}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \ <a
                href="<c:url value="${deleted}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="received"><c:param name="orderby" value="received"/></c:url>
        <th>Received <a href="<c:url value="${received}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \ <a
                href="<c:url value="${received}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="sendAttempts"><c:param name="orderby" value="sendAttempts"/></c:url>
        <th>SendAttempts <a href="<c:url value="${sendAttempts}"><c:param name="asc" value = "true"/></c:url>">
            &#9650;</a> \ <a href="<c:url value="${sendAttempts}"><c:param name="asc" value = "false"/></c:url>">
            &#9660;</a></th>
        <c:url value="${order}" var="sendAttemptsMax"><c:param name="orderby" value="sendAttemptsMax"/></c:url>
        <th>SendAttemptsMax <a href="<c:url value="${sendAttemptsMax}"><c:param name="asc" value = "true"/></c:url>">
            &#9650;</a> \ <a href="<c:url value="${sendAttemptsMax}"><c:param name="asc" value = "false"/></c:url>">
            &#9660;</a></th>
        <c:url value="${order}" var="nextAttempt"><c:param name="orderby" value="nextAttempt"/></c:url>
        <th>NextAttempt <a href="<c:url value="${nextAttempt}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a>
            \ <a href="<c:url value="${nextAttempt}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
    </tr>
    <c:forEach var="o" items="${table}">
        <tr>
            <td>${o.messageId}</td>
            <td>${o.messageStatus}</td>
            <td>${o.notificationStatus}</td>
            <td>${o.mshRole}</td>
            <td>${o.messageType}</td>
            <td>${o.deleted}</td>
            <td>${o.received}</td>
            <td>${o.sendAttempts}</td>
            <td>${o.sendAttemptsMax}</td>
            <td>${o.nextAttempt}</td>
        </tr>
    </c:forEach>
</table>
<c:if test="${page>0}">
    <!-- Pagination -->
    <div id="pagination">
        <c:if test="${page<=pages}">
            <c:if test="${page>1}"><span><a
                    href="<c:url value="${toPage}"><c:param name="page" value="${page-1}"/></c:url>">
                &lt;&lt;prev</a></span></c:if>
            <c:forEach var="i" begin="${beginIndex}" end="${endIndex}">
                <c:choose>
                    <c:when test="${i==page}"><span class="currient">${page}</span></c:when>
                    <c:otherwise><span><a
                            href="<c:url value="${toPage}"><c:param name="page" value="${i}"/></c:url>">${i}</a></span></c:otherwise>
                </c:choose>
            </c:forEach>
            <c:if test="${page<pages}"><span><a
                    href="<c:url value="${toPage}"><c:param name="page" value="${page+1}"/></c:url>">next&gt;&gt;</a></span></c:if>
        </c:if>
    </div>
</c:if>
</body>
</html>