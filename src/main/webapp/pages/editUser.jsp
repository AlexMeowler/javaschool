<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Edit user</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<c:url value="${editUser}" var = "edit"/>
	<form:form id = "form" action="${edit}" method="POST">
		<br>
		<input type="text" name="id" value="${user.id}" hidden = "true"/>
		<label>Login</label>
		<input type="text" name="login" value="${user.login}"/>
		<span class = "error">${error_login}</span>
		<span class = "error">${error_unique}</span>
		<br>
		<label>Password (leave empty for no change)</label>
		<input type="password" name="password"/>
		<span class = "error">${error_realPassword}</span>
		<br>
		<c:if test="${we.role == 'admin'}">
		<label>Role</label>
		<select name = "role">
			<option value = "admin" ${user.role == 'admin' ? 'selected' : ''}>Administrator</option>
			<option value = "manager" ${user.role == 'manager' ? 'selected' : ''}>Manager</option>
			<option value = "driver" ${user.role == 'driver' ? 'selected' : ''}>Driver</option>
		</select>
		<br>
		</c:if>
		<label>Name</label>
		<input type="text" name="name" value="${user.userInfo.name}"/>
		<span class = "error">${error_name}</span>
		<br>
		<label>Surname</label>
		<input type="text" name="surname" value="${user.userInfo.surname}"/>
		<span class = "error">${error_surname}</span>
		<br>
		<label>Status</label>
		<select name = "status">
			<option value = "resting" ${user.userInfo.status == 'resting' ? 'selected' : ''}>Resting</option>
			<option value = "on_shift" ${user.userInfo.status == 'on_shift' ? 'selected' : ''}>On shift</option>
			<option value = "driving" ${user.userInfo.status == 'driving' ? 'selected' : ''}>Driving</option>
		</select>
		<br>
		<label>City</label>
		<input type="text" name="currentCity" value="${user.userInfo.currentCity}"/>
		<span class = "error">${error_currentCity}</span>
		<br>
		<input type="submit" value="Finish editing user">
	</form:form>	
	</div>
</body>
</html>
