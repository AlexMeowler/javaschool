<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en">
<head>
	<title>Access Denied</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
		<h1>Access Denied</h1>
		<img src = "<c:url value="static/HAL9000.png"/>" alt="HAL9000 image" width = "110" height = "318">
		<c:set var = "message" value = ""/>
		<c:if test="${not empty username}"><c:set var = "message" value = "${username}, "/></c:if>
		<h2>Sorry, <c:out value="${message}"/>but I'm afraid can't let you do that...</h2>
		<a href="${pageContext.request.contextPath}/home">Return to home page</a>
	</div>
</body>
</html>