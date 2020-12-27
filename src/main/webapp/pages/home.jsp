<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Logiweb</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
    	<sec:authorize access = "isAuthenticated()">
			<p>Welcome to home page, <sec:authentication property="principal.username"/>!</p>
		</sec:authorize>
		<sec:authorize access = "!isAuthenticated()">
		<c:url value="/spring_auth" var = "loginURL"/>
		<form:form class = "login-form" action="${loginURL}" method="POST">
			<h2 class = "form-signin-heading">Please log in</h2>
			<div class="form-group">
				<label>Login</label>
				<input class="form-control form-control-lg" type="text" name="j_login"/>
			</div>
			<div class="form-group">
				<label>Password</label>
				<input class="form-control form-control-lg" type="password" name="j_password"/>
			</div>
			<input class = "btn btn-primary btn-large" type="submit" value="Log in">
		</form:form>
		</sec:authorize>
		<c:out value="${message}"/>
  	</div>
</body>
</html>
