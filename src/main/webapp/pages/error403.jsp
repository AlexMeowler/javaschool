<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<body>
	<h1>Access Denied</h1>
	<img src = "<c:url value="static/HAL9000.png"/>" width = "110" height = "318">
	<c:set var = "message" value = ""/>
	<c:if test="${not empty username}"><c:set var = "message" value = "${username}, "/></c:if>
	<!-- add hal9000 image -->
	<h2>Sorry, <c:out value="${message}"/>but I can't let you do that...</h2>
	<a href="${pageContext.request.contextPath}/home">Return to home page</a>
</body>
</html>