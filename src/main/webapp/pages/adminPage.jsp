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
	<p>Welcome, administrator <sec:authentication property="principal.username" />!</p>
	<p>Here is user list:</p>
	<table>
		<tr>
			<td>ID</td>
			<td>Login</td>
			<td>Password</td>
			<td>Role</td>
		</tr>
		<c:forEach var="user" items="${userList}">
		<tr>
			<td>${user.id}</td>
			<td>${user.login}</td>
			<td>${user.password}</td>
			<td>${user.role}</td>
		</tr>
		</c:forEach>
	</table>
</body>
</html>
