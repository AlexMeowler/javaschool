<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!doctype html>
<html lang="en">
<head>
    <title>Access Denied</title>
    <link rel="shortcut icon" href="<c:url value="static/icon.png"/>" type="image/png">
    <link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
</head>
<body>
	<jsp:include page="menu.jsp" />
	<c:set var="message" value="" />
	<c:if test="${not empty username}"><c:set var="message" value="${username}, " /></c:if>
	<div class="container main-body">
		<div class="row">
			<div class="col-lg-2 col-md-2 col-sm-12">
				<img src="<c:url value="static/HAL9000.png"/>" alt="HAL9000 image" width="110" height="318">
			</div>
			<div class="col-lg-10 col-md-10 col-sm-12 desc">
				<h2>Access Denied</h2>
				<h3>"I'm sorry, <c:out value="${message}" />I'm afraid I can't do that..."</h3>
				<p>If you see this page, then you tried to access something you can not. Please don't do that.</p>
				<a href="${pageContext.request.contextPath}/home">Return to home page</a>
			</div>
		</div>
		<div class="footer"></div>
	</div>
</body>
</html>