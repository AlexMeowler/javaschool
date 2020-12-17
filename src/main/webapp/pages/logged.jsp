<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
	Logged successfully<br>User list:
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
