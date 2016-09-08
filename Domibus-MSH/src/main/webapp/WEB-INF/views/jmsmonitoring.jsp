<%@page session="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<html>
<head>
    <style type="text/css">

    </style>


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
        <tr class="row"><td>Source:</td><td colspan="2">
            <select name="source">
                <c:forEach items="${destinationMap}" var="destination">
                    <option value="${destination.key}"
                            <c:if test="${destination.key == source}">
                                <c:out value="selected"/>
                            </c:if>
                    >
                        <c:choose>
                            <c:when test="${destination.value.internal == true}">
                                [internal] ${destination.key}
                            </c:when>
                            <c:otherwise>
                                  ${destination.key}
                            </c:otherwise>
                        </c:choose>
                        (
                        ${destination.value.numberOfMessages}
                        )
                    </option>
                </c:forEach>
            </select>
        </td></tr>
        <tr class="row">
            <td>Period:</td>
            <td colspan="2">
                <input id="From" name="fromDate" size="16" value="${fromDate}"/>&nbsp;-&nbsp;
                <input id="To" name="toDate" size="16" value="${toDate}"/>
            </td>
        </tr>
        <tr class="row">
            <td>JMS type:</td>
            <td colspan="2"><input name="jmsType" size="120" value="${jmsType}"></td>
        </tr>
        <tr class="row">
            <td title="JMS message selector expression(for syntax please refer to https://docs.oracle.com/cd/E19798-01/821-1841/bncer/index.html)">Selector:</td>
            <td colspan="2"><input name="selector" size="120" value="${selector}"></td>
        </tr>
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
    <input type="hidden" name="fromDate" value="${fromDate}">
    <input type="hidden" name="toDate" value="${toDate}">
    <input type="hidden" name="jmsType" value="${jmsType}">
    <input type="hidden" name="selector" value="${selector}">

    <input id="NewButton" type="submit" name="action" value="New" style="display: none;"/>
    <input id="ResendButton" type="submit" name="action" value="Resend" style="display: none;" onclick="return validateForm(this);">
    <input id="MoveButton" type="submit" name="action" value="Move" onclick="return validateForm(this);"/>
    <input id="RemoveButton" type="submit" name="action" value="Remove" onclick="return validateForm(this);"/>
    <table border="0" width="100%">
        <tr class="row_high">
            <td><input type="checkbox" id="selectAll" onclick="checkAll(this)"/></td>
            <td><b>Id</b></td>
            <td><b>JMS type</b></td>
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
                <td valign="top" width="10%" nowrap><a href="jmsmessage?action=View&source=${source}&selectedMessages=${message.id}&fromDate=${fromDate}&toDate=${toDate}&jmsType=${jmsType}&selector=${selector}">${message.id}</a></td>
                <td valign="top" width="30%">${message.type}</td>
                <td valign="top" width="10%" nowrap><fmt:formatDate value="${message.timestamp}" pattern="yyyy-MM-dd HH:mm:ss.SSS" /></td>
                <td valign="top" width="100%">${message.content}</td>
                <td valign="top" width="1%">${message.customProperties}</td>
                <td valign="top" width="1%">${message.JMSProperties}</td>
            </tr>
        </c:forEach>
    </table>
</form>
</body>
</html>