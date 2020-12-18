<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<body>
	<table>
	<tr>
		<td><a href="/logiweb/home">Home</a></td>
		<td><a href="/logiweb/adminPage">Administrator page</a></td>
		<td><a href="/logiweb/managerPage">Manager page</a></td>
		<td><a href="/logiweb/driverPage">Driver page</a></td>
		<td>
			<sec:authorize access = "isAuthenticated()">
			<c:url value="/logout" var = "logoutURL"/>
			<form:form action="${logoutURL}" method="POST">
				<input type="submit" value="Log out">
			</form:form>
			</sec:authorize>
		</td>
	</tr>
	</table>
	<sec:authorize access = "isAuthenticated()">
	<p>Welcome to home page, <sec:authentication property="principal.username"/>!</p>
	</sec:authorize>
	<sec:authorize access = "!isAuthenticated()">
	<c:url value="/spring_auth" var = "loginURL"/>
	<form:form action="${loginURL}" method="POST">
		<label>Login</label>
		<input type="text" name="j_login"/>
		<label>Password</label>
		<input type="password" name="j_password"/>
		<input type="submit" value="Log in">
	</form:form>
	</sec:authorize>
	<c:out value="${message}"/>
</body>
</html>
