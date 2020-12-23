<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<p>Welcome, administrator <sec:authentication property="principal.username" />!</p>
	<p>Here is user list:</p>
	<table>
		<tr>
			<td>ID</td>
			<td>Login</td>
			<td>Name</td>
			<td>Surname</td>
			<td>Role</td>
			<td>Hours worked in current month</td>
			<td>Status</td>
			<td>Location</td>
			<td>Edit</td>
			<td>Delete</td>
		</tr>
		<c:forEach var="user" items="${userList}">
		<tr>
			<td>${user.id}</td>
			<td>${user.login}</td>
			<td>${user.userInfo.name}</td>
			<td>${user.userInfo.surname}</td>
			<td>${user.role}</td>
			<td>${user.userInfo.hoursWorked}</td>
			<td>${user.userInfo.status}</td>
			<td>${user.userInfo.currentCity}</td>
			<td><!-- add edit link here --></td>
			<td><a href="${pageContext.request.contextPath}/deleteUser/${user.id}">Delete user</a></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm()">Add new user</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visible}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewUser" var = "addUser"/>
	<form:form id = "form" action="${addUser}" method="POST" style = "${hidden}">
		<br>
		<label>Login</label>
		<input type="text" name="login"/>
		<label>Password</label>
		<input type="password" name="password"/>
		<label>Role</label>
		<select name = "role">
			<option value = "admin" selected>Administrator</option>
			<option value = "manager">Manager</option>
			<option value = "driver">Driver</option>
		</select>
		<br>
		<label>Name</label>
		<input type="text" name="name"/>
		<label>Surname</label>
		<input type="text" name="surname"/>
		<input type="submit" value="Add user">
	</form:form>	
	</div>
	
</body>
</html>
