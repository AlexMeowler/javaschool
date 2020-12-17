<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<body>
	<table>
		<tr>
			<td><a href="/logiweb/home">Home</a></td>
			<td><a href="/logiweb/logged">Page for logged people</a></td>
			<td>
				<c:url value="/logout" var = "logoutURL"/>
				<form:form action="${logoutURL}" method="POST">
					<input type="submit" value="Log out">
				</form:form>
			</td>
		</tr>
	</table>
	Logged successfully, <sec:authentication property="principal.username" />!<br>User list:
	<table>
		<tr>
			<th>User name</th>
			<th>User role</th>
		</tr>
		<c:forEach var="user" items="${userList}">
		<tr>
			<td>${user.login}</td>
			<td>${user.role}</td>
		</tr>
		</c:forEach>
	</table>
</body>
</html>
