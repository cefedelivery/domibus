<%@page session="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<html>
<head>
    <jsp:include page="header.jsp"/>

    <script>
        function toggleOtherDestination(chosen) {
            if (chosen == 'Other') {
                document.getElementById('OtherDestination').style.visibility = 'visible';
            } else {
                document.getElementById('OtherDestination').style.visibility = 'hidden';
            }
        }
    </script>
</head>

<body>


<h1>JMS Message</h1>
<form name="messageForm" method="post" action="jmsmessage/action">
    <input type="hidden" name="source" value="${source}"/>
    <input type="hidden" name="fromDate" value="${fromDate}">
    <input type="hidden" name="to" value="${to}">
    <input type="hidden" name="jmsType" value="${jmsType}">
    <input type="hidden" name="selector" value="${selector}">

    <table border="0" width="100%">

    <c:choose>
        <c:when test="${multiMessage}">
            <tr class="row"><td valign="top" width="100">Source:</td><td>${source}</td></tr>
            <tr class="row">
                <td valign="top" width="100">Id:</td>
                <td>
                    <c:forEach items="${selectedMessages}" var="id">
                        <input type="hidden" name="selectedMessages" value="${id}"/>
                        <c:out value="${id} "/>
                    </c:forEach>
                </td>
            </tr>
        </c:when>
        <c:otherwise>
            <c:if test="${action != 'New'}">
                <tr class="row"><td valign="top" width="100">Source:</td><td>${source}</td></tr>
                <tr class="row">
                    <td valign="top" width="100">Id:</td>
                    <td>
                        <c:forEach items="${selectedMessages}" var="id">
                            <input type="hidden" name="selectedMessages" value="${id}"/>
                            <c:out value="${id} "/>
                        </c:forEach>
                    </td>
                </tr>
                <tr class="row">
                    <td valign="top" width="100">Timestamp:</td>
                    <td>
                        <c:if test="${not empty message.timestamp}">
                            <fmt:formatDate value="${message.timestamp}" pattern="yyyy-MM-dd HH:mm:ss.SSS" />
                        </c:if>
                    </td>
                </tr>

            </c:if>

            <tr class="row">
                <td valign="top" width="100">JMS type:</td>
                <td><input name="type" size="132" value="${message.type}"/></td>
            </tr>
            <tr class="row">
                <td valign="top" width="100">Properties:</td>
                <td>
                    <table border="0" width="100%">
                        <c:forEach items="${message.JMSProperties}" var="jmsProperty">
                            <tr class="row">
                                <td>${jmsProperty.key}</td>
                                <td>${jmsProperty.value}</td>
                            </tr>
                        </c:forEach>
                        <tr class="row">
                            <td colspan="2">
                                <textarea cols="130" rows="5" name="customProperties">${message.customProperties}</textarea>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr class="row">
                <td valign="top" width="100">Content:</td>
                <td>
                    <textarea cols="132" rows="20" name="content">${message.content}</textarea>
                </td>
            </tr>
            </c:otherwise>
    </c:choose>


    </table>

        <table border="0" width="100%">
            <c:if test="${action != 'View' && action != 'Remove'}">
                <tr class="row">
                    <td valign="top" width="100">destination:</td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty originalQueue}">
                                <input type="hidden" name="destinationKey" value="${originalQueue}"/>
                                <c:out value="${originalQueue} "/>
                            </c:when>
                            <c:otherwise>
                                <select name="destinationKey"
                                        onchange="toggleOtherDestination(document.messageForm.destinationKey.options[document.messageForm.destinationKey.selectedIndex].value);">
                                    <c:forEach items="${destinationMap}" var="destination">
                                        <option>${destination.key}</option>
                                    </c:forEach>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:if>

            <c:if test="${action != 'View'}">
                <c:set var="detailAction" value="''"/>

                <c:choose>
                    <c:when test="${action == 'New' || action == 'Resend'}">
                        <c:set var="detailAction" value="send"/>
                    </c:when>
                    <c:when test="${action == 'Move' || action == 'Remove'}">
                        <c:set var="detailAction" value="${fn:toLowerCase(action)}"/>
                    </c:when>
                </c:choose>

                <tr>
                    <td colspan="2"><input type="submit" name="action" value="${detailAction}"></td>
                </tr>
            </c:if>

        </table>

</form>

<a href="jmsmonitoring?source=${source}&fromDate=${fromDate}&toDate=${toDate}&jmsType=${jmsType}&selector=${selector}">Return to JMS Monitoring</a>
</body>
</html>
