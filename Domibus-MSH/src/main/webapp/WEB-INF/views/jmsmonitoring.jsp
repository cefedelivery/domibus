<%@page session="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<html>
<head>
    <jsp:include page="header.jsp"/>

    <script>
        function checkAll(master) {
            var allRows = document.getElementsByName("selectedMessages");
            for (var i = 0; i < allRows.length; i++) {
                if (allRows[i].type == "checkbox") {
                    allRows[i].checked = master.checked;
                }
            }
        }
        function validateForm() {
            var allRows = document.getElementsByName("selectedMessages");
            var okay = false;
            for (var i = 0; i < allRows.length; i++) {
                if (allRows[i].type == "checkbox" && allRows[i].checked) {
                    okay = true;
                    break;
                }
            }
            if (!okay) {
                alert("Select at least one message");
            }
            return okay;
        }

    </script>




</head>
<body>
<h1>Domibus - JMS Monitoring</h1>


<form name="filterForm" method="post" action="jmsmonitoring">
    <input type="hidden" name="action" value="filter"/>
    <table border="0" width="100%">
        <tr class="row"><td>source:</td><td colspan="2">
            <select name="source">
                <c:forEach items="${destinationMap}" var="destination">
                    <option value="${destination.key}"
                            <c:if test="${destination.key == source}">
                                <c:out value="selected"/>
                            </c:if>
                    >
                    ${destination.key}
                        (
                        ${destination.value.numberOfMessages}
                        <c:choose>
                            <c:when test="${destination.value.numberOfMessagesPending} > 0">
                                , ${destination.value.numberOfMessagesPending} pending
                            </c:when>
                        </c:choose>
                        )
                    </option>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="row"><td>period:</td><td colspan="2"><input id="From" name="from" size="16" value="${fromDate}"/>&nbsp;-&nbsp;<input id="To" name="to" size="16" value="${toDate}"/></td></tr>
        <tr class="row"><td>type:</td><td colspan="2"><input name="type" size="120" value="${jmsType}"></td></tr>
        <tr class="row"><td>selector:</td><td colspan="2"><input name="selector" size="120" value="${selector}"></td></tr>
    </table>
    <table>
        <tr class="row">
            <td><input type="submit" value="Search"></td>
            <td colspan="2">
                <c:choose>
                    <c:when test="${not empty messages}">
                        ${messages.size()}
                    </c:when>
                    <c:otherwise>
                        0
                    </c:otherwise>
                </c:choose>
                results
            </td>
        </tr>
    </table>
</form>

<form name="eventsForm" method="post" action="jmsmessage">
    <input type="hidden" name="source" value="${source}">
    <input id="NewButton" type="submit" name="action" value="New"/>
    <input id="ResendButton" type="submit" name="action" value="Resend" onclick="return validateForm(this);">
    <input id="MoveButton" type="submit" name="action" value="Move" onclick="return validateForm(this);"/>
    <input id="RemoveButton" type="submit" name="action" value="Remove" onclick="return validateForm(this);"/>
    <table border="0" width="100%">
        <tr class="row_high">
            <td><input type="checkbox" id="selectAll" onclick="checkAll(this)"/></td>
            <td><b>Id</b></td>
            <td><b>Type</b></td>
            <td><b>Time</b></td>
            <td><b>Content</b></td>
            <td><b>Custom properties</b></td>
            <td><b>JMS properties</b></td>
        </tr>
        <c:forEach items="${messages}" var="message">
            <tr class="row">
                <td width="5%" valign="top">
                    <input type="checkbox" name="selectedMessages" value="${message.id}"/>
                </td>
                <td valign="top" width="10%" nowrap><a href="jmsmessage?action=View&source=${source}&selectedMessages=${message.id}">${message.id}</a></td>
                <td valign="top" width="30%" class="tooltip" alt="${message.type}">${message.type}</td>
                <td valign="top" width="10%" nowrap><fmt:formatDate value="${message.timestamp}" pattern="yyyy-MM-dd HH:mm:ss.SSS" /></td>
                <td valign="top" width="100%" class="tooltip" alt="${fn:escapeXml(message.content)}">${message.content}</td>
                <td valign="top" width="1%">${message.customProperties}</td>
                <td valign="top" width="1%">${message.JMSProperties}</td>
            </tr>
        </c:forEach>
    </table>
</form>
</body>
</html>