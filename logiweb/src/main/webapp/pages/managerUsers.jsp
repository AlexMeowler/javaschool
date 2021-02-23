<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Manager page (user editing)</title>
	<link rel="shortcut icon" href="<c:url value="/static/icon.png"/>" type="image/png">
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="/static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<h3>Welcome, manager ${current_user_name}!</h3>
	<span class = "error">${error_userDeletionFailed}</span>
	<p>Drivers list:</p>
	<table class="info-table">
		<caption hidden="true">Drivers list</caption>
		<tr>
			<th scope="col">ID</th>
			<th scope="col">Login</th>
			<th scope="col">Name</th>
			<th scope="col">Surname</th>
			<th scope="col">Hours worked in current month</th>
			<th scope="col">Status</th>
			<th scope="col">Location</th>
			<th scope="col">Edit</th>
			<th scope="col">Delete</th>
		</tr>
		<c:forEach var="driver" items="${driverList}">
		<tr>
			<td>${driver.id}</td>
			<td>${driver.login}</td>
			<td>${driver.userInfo.name}</td>
			<td>${driver.userInfo.surname}</td>
			<td>${driver.userInfo.hoursWorked}</td>
			<td>${driver.userInfo.status}</td>
			<td>${driver.userInfo.city.currentCity}</td>
			<td><a href="<c:url value="/editDriver/${driver.id}"/>">Edit driver</a></td>
			<td><a href="<c:url value="/deleteDriver/${driver.id}"/>">Delete driver</a></td>
		</tr>
		</c:forEach>
		<tr>
			<td style="visibility:hidden; border-bottom-color: rgba(0, 0, 0, 0.0)" colspan="9">Empty row</td>
		</tr>
		<tr>
			<td style="border-color: rgba(0, 0, 0, 0.0)"><input type="submit" value="Previous page" onclick="window.location='<c:url value="/managerUsersPage/${page - 1}"/>';" /></td>
			<td colspan="7" style="border-color: rgba(0, 0, 0, 0.0)">Page ${page} of ${maxPage}</td>
			<td style="border-color: rgba(0, 0, 0, 0.0)"><input type="submit" value="Next page" onclick="window.location='<c:url value="/managerUsersPage/${page + 1}"/>';" /></td>
		</tr>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('driverform')">Add new driver</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visibledriver}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewDriver" var = "addDriver"/>
	<br><span class = "error">${error_userMaliciousInput}</span>
	<form:form id = "driverform" action="${addDriver}" method="POST" style = "${hidden}">
		<br>
		<table class="form-table">
		<caption hidden="true">form</caption>
        <tr hidden="true"><th scope="col">Hidden header</th></tr>
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
		<input type="submit" value="Add driver">
	</form:form>
	<div class="footer"></div>
	</div>
</body>
</html>