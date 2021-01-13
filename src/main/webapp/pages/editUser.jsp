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
		<table class="form-table">
		<tr>
			<td><input type="text" name="id" value="${user.id}" hidden = "true"/></td>
		</tr>
		<tr>
			<td><label>Login</label></td>
			<td><input type="text" name="login" value="${user.login}"/></td>
			<td>
				<span class = "error">${error_login}</span>
				<span class = "error">${error_unique}</span>
			</td>
		</tr>
		<tr>
			<td><label>Password</label></td>
			<td><input type="password" name="password"/></td>
			<td><span class = "error">${error_realPassword}</span></td>
		</tr>
		<c:if test="${we.role == 'admin'}">
		<tr>
			<td><label>Role</label></td>
			<td><select name = "role">
					<option value = "admin" ${user.role == 'admin' ? 'selected' : ''}>Administrator</option>
					<option value = "manager" ${user.role == 'manager' ? 'selected' : ''}>Manager</option>
					<option value = "driver" ${user.role == 'driver' ? 'selected' : ''}>Driver</option>
				</select>
			</td>
		</tr>
		</c:if>
		<tr>
			<td><label>Name</label></td>
			<td><input type="text" name="name" value="${user.userInfo.name}"/></td>
			<td><span class = "error">${error_name}</span></td>
		</tr>
		<tr>
			<td><label>Surname</label></td>
			<td><input type="text" name="surname" value="${user.userInfo.surname}"/></td>
			<td><span class = "error">${error_surname}</span></td>
		</tr>
		<tr>
			<td><label>Status</label></td>
			<td><select name = "status">
					<option value = "resting" ${user.userInfo.status == 'resting' ? 'selected' : ''}>Resting</option>
					<option value = "on shift" ${user.userInfo.status == 'on shift' ? 'selected' : ''}>On shift</option>
					<option value = "driving" ${user.userInfo.status == 'driving' ? 'selected' : ''}>Driving</option>
				</select>
			</td>
			<td><span class = "error">${error_status}</span></td>
		</tr>
		<tr>
			<td><label>City</label></td>
			<td><select name = "currentCity">
					<c:forEach var="city" items="${cityList}">
						<option value = "${city.currentCity}" ${user.userInfo.city.currentCity == city.currentCity ? 'selected' : ''}>${city.currentCity}</option>
					</c:forEach>
				</select>
			</td>
			<td><span class = "error">${error_currentCity}</span></td>
		</tr>
		</table>
		<input type="submit" value="Finish editing user">
	</form:form>	
	</div>
</body>
</html>
