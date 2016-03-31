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
    <c:url value="/home/errorlog" var="filterederrorlog">
        <c:if test="${not empty param.errorSignalMessageId}"><c:param name="errorSignalMessageId"
                                                                      value="${param.errorSignalMessageId}"/></c:if>
        <c:if test="${not empty param.mshRole}"><c:param name="mshRole" value="${param.mshRole}"/></c:if>
        <c:if test="${not empty param.messageInErrorId}"><c:param name="messageInErrorId"
                                                                  value="${param.messageInErrorId}"/></c:if>
        <c:if test="${not empty param.errorCode}"><c:param name="errorCode" value="${param.errorCode}"/></c:if>
        <c:if test="${not empty param.errorDetail}"><c:param name="errorDetail" value="${param.errorDetail}"/></c:if>
        <c:if test="${not empty param.timestampFrom}"><c:param name="timestampFrom"
                                                               value="${param.timestampFrom}"/></c:if>
        <c:if test="${not empty param.timestampTo}"><c:param name="timestampFrom" value="${param.timestampTo}"/></c:if>
        <c:if test="${not empty param.notifiedFrom}"><c:param name="notifiedFrom" value="${param.notifiedFrom}"/></c:if>
        <c:if test="${not empty param.notifiedTo}"><c:param name="notifiedTo" value="${param.notifiedTo}"/></c:if>
    </c:url>
    <!-- paging URLs -->
    <c:url value="${filterederrorlog}" var="toPage">
        <c:param name="size" value="${size}"/>
        <c:if test="${not empty column}">
            <c:param name="orderby" value="${column}"/>
            <c:param name="asc" value="${asc}"/>
        </c:if>
    </c:url>
    <!-- table resize URL -->
    <c:url value="${filterederrorlog}" var="resizeTable">
        <c:param name="page" value="${page}"/>
        <c:if test="${not empty column}">
            <c:param name="orderby" value="${column}"/>
            <c:param name="asc" value="${asc}"/>
        </c:if>
    </c:url>
    <!-- order URLs -->
    <c:url value="${filterederrorlog}" var="order">
        <c:param name="page" value="${page}"/>
        <c:param name="size" value="${size}"/>
    </c:url>
</head>
<body>
<h1>${title}</h1>

<div id="filtermask">
    <form action="errorlog" method="get">
        <h3>Filter:</h3>
        ErrorSignalMessageId: <input name="errorSignalMessageId" type="text" size="30" maxlength="30">
        MshRole: <select name="mshrole" size="1">
        <option/>
        <c:forEach var="msh" items="${mshrolevalues}">
            <option>${msh}</option>
        </c:forEach></select>
        messageInErrorId: <input name="messageInErrorId" type="text" size="30" maxlength="30">
        ErrorCode: <select name="errorCode" size="1">
        <option/>
        <c:forEach var="ec" items="${errorCodevalues}">
            <option>${ec}</option>
        </c:forEach></select>
        ErrorDetail: <input name="errorDetail" type="text" size="30" maxlength="30">
        <br/>
        <nobr><h4>Timestamp:</h4> From: <input name="timestampFrom" type="text" size="20" maxlength="30"> To: <input
                name="timestampTo" type="text" size="20" maxlength="30"></nobr>
        <br/>
        <nobr><h4>Notified:</h4> From: <input name="notifiedFrom" type="text" size="20" maxlength="30"> To: <input
                name="notifiedTo" type="text" size="20" maxlength="30"></nobr>
        <input name="size" type="hidden" value="${size}">
        <input type="submit" value="Search">
    </form>
</div>
<div>
    Rows:
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
<table>
    <tr>
        <c:url value="${order}" var="errorSignalMessageId"><c:param name="orderby"
                                                                    value="errorSignalMessageId"/></c:url>
        <th>ErrorSignalMessageId <a
                href="<c:url value="${errorSignalMessageId}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \
            <a href="<c:url value="${errorSignalMessageId}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a>
        </th>
        <c:url value="${order}" var="mshRole"><c:param name="orderby" value="mshRole"/></c:url>
        <th>MshRole <a href="<c:url value="${mshRole}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \ <a
                href="<c:url value="${mshRole}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="messageInErrorId"><c:param name="orderby" value="messageInErrorId"/></c:url>
        <th>MessageInErrorId <a href="<c:url value="${messageInErrorId}"><c:param name="asc" value = "true"/></c:url>">
            &#9650;</a> \ <a href="<c:url value="${messageInErrorId}"><c:param name="asc" value = "false"/></c:url>">
            &#9660;</a></th>
        <c:url value="${order}" var="errorCode"><c:param name="orderby" value="errorCode"/></c:url>
        <th>ErrorCode <a href="<c:url value="${errorCode}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \
            <a href="<c:url value="${errorCode}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="errorDetail"><c:param name="orderby" value="errorDetail"/></c:url>
        <th>ErrorDetail <a href="<c:url value="${errorDetail}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a>
            \ <a href="<c:url value="${errorDetail}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="timestamp"><c:param name="orderby" value="timestamp"/></c:url>
        <th>Timestamp <a href="<c:url value="${timestamp}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \
            <a href="<c:url value="${timestamp}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
        <c:url value="${order}" var="notified"><c:param name="orderby" value="notified"/></c:url>
        <th>Notified <a href="<c:url value="${notified}"><c:param name="asc" value = "true"/></c:url>">&#9650;</a> \ <a
                href="<c:url value="${notified}"><c:param name="asc" value = "false"/></c:url>">&#9660;</a></th>
    </tr>
    <c:forEach var="o" items="${table}">
        <tr>
            <td>${o.errorSignalMessageId}</td>
            <td>${o.mshRole}</td>
            <td>
                <a href="<c:url value="/home/messagelog"><c:param name="messageId" value="${o.messageInErrorId}"/></c:url>">${o.messageInErrorId}</a>
            </td>
            <td>${o.errorCode}</td>
            <td>${o.errorDetail}</td>
            <td>${o.timestamp}</td>
            <td>${o.notified}</td>
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