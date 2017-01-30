<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
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

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>${title}</title>
    <style type="text/css">
        body {
            margin: 0.2em;
        }

        li {
            list-style: none;
            display: inline;
            margin: 0.4em;
        }

        table {
            border-collapse: collapse;
        }

        table, td, th {
            border: 1px solid black;
            empty-cells: show;
            padding: 0.2em;
            margin: 1em;
        }

        th {
            background-color: gray;
            color: white;
        }

		tr.messageLogTr {
			font-size: .93em;
		}

        fieldset {
            border: 1px solid black;
            padding: 0.2em;
            margin: 1em;
        }

        legend {
            padding: 0.2em;
            margin: 1em;
        }

        #filtermask {
            border: 1px solid black;
            padding: 0.2em;
            margin: 1em;
        }

        div#pagination {
            font-family: tahoma;
            text-align: center;
        }

        #pagination span {
            display: block;
            float: left;
            font-size: 12px;
            line-height: 13px;
            margin: 2px 6px 2px 0;
        }

        #pagination span a {
            background-color: #ffffff;
            border: 1px solid #bbbbbb;
            color: #303030;
            display: block;
            padding: 1px 5px 2px 5px;
            text-decoration: none;
        }

        #pagination span a:hover, #pagination span a:active {
            background-color: #bbbbbb;
            border: 1px solid #303030;
        }

        #pagination span.currient {
            background-color: #303030;
            border: 1px solid #303030;
            color: #ffffff;
            font-size: 11px;
            padding: 1px 5px 2px 5px;
        }
    </style>

    <c:url value="/j_spring_security_logout" var="logoutUrl"/>

    <!-- csrt support -->
    <form action="${logoutUrl}" method="post" id="logoutForm">
        <input type="hidden"
               name="${_csrf.parameterName}"
               value="${_csrf.token}"/>
    </form>

    <script>
        function formSubmit() {
            document.getElementById("logoutForm").submit();
        }
    </script>

    <c:if test="${pageContext.request.userPrincipal.name != null}">
        <ul>
            <li>
                <c:url value="/home" var="home">
                </c:url>
                <a href="${home}">Home</a>

            </li>
            <li>
                <c:url value="/home/messagelog" var="messagelog">
                </c:url>
                <a href="${messagelog}">Message Log</a>
            </li>
            <li>
                <c:url value="/home/messagefilter" var="messagefilter">
                </c:url>
                <a href="${messagefilter}">Message Filter</a>
            </li>
            <li>
                <c:url value="/home/errorlog" var="errorlog">
                </c:url>
                <a href="${errorlog}">Error Log</a>
            </li>
            <sec:authorize ifAllGranted="ROLE_ADMIN">
                <li>
                    <c:url value="/home/updatepmode" var="updatepmode">
                    </c:url>
                    <a href="${updatepmode}">Configuration upload</a>

                </li>
            </sec:authorize>
            <sec:authorize ifAllGranted="ROLE_ADMIN">
                <li>
                    <c:url value="/home/jmsmonitoring" var="jmsmonitoring">
                    </c:url>
                    <a href="${jmsmonitoring}">JMS Monitoring</a>

                </li>
            </sec:authorize>
            <li>
                <a href="javascript:formSubmit()"> Logout</a>
            </li>
        </ul>
    </c:if>
</head>