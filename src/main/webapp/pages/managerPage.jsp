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
	<p>Welcome to protected manager page!</p>
</body>
</html>