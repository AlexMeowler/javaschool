<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!doctype html>
<html lang="en">
<head>
<title>Error</title>
<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
</head>
<body>
	<jsp:include page="menu.jsp" />
	<div class="container main-body">
		<div class="row">
			<div class="col-lg-4 col-md-4 col-sm-12">
				<img src="<c:url value="static/sadcat.jpg"/>" alt="crying cat face" width="350" height="197">
				<div class="errorCode">${errorCode}</div>
			</div>
			<div class="col-lg-8 col-md-8 col-sm-12 desc">
				<h2>Oops...</h2>
				<p>If you see this page, then something incredibly bad has happened on our side. 
				   Don't worry, most likely it is not your fault. 
				   Our specialists are working on the problem.</p>
				<a href="${pageContext.request.contextPath}/home">Return to home page</a>
			</div>
		</div>
		<div class="footer"></div>
	</div>
</body>
</html>