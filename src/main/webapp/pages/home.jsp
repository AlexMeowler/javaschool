<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
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
	<c:url value="/spring_auth" var = "loginURL"/>
	<form:form action="${loginURL}" method="POST">
		<label>Login</label>
		<input type="text" name="j_login"/>
		<label>Password</label>
		<input type="password" name="j_password"/>
		<input type="submit" value="Log in">
	</form:form>
	<c:out value="${message}"/>
</body>
</html>
