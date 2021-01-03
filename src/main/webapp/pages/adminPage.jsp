<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Administrator page</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<p>Welcome, administrator <sec:authentication property="principal.username" />!</p>
	<c:url value="/addCityInfo" var = "addCityInfo"/>
	<form:form action="${addCityInfo}" method="POST">
		<input type="submit" value="Add city information to database">
	</form:form>
	<br>
	<c:url value="/addDriverInfo" var = "addDriverInfo"/>
	<form:form action="${addDriverInfo}" method="POST">
		<input type="submit" value="Add drivers information to database">
	</form:form>
	<br>
	<c:url value="/addCarsInfo" var = "addCarsInfo"/>
	<form:form action="${addCarsInfo}" method="POST">
		<input type="submit" value="Add cars information to database">
	</form:form>
	<p>Here is user list:</p>
	<table>
		<caption hidden="true">User list</caption>
		<tr>
			<th scope="col">ID</th>
			<th scope="col">Login</th>
			<th scope="col">Name</th>
			<th scope="col">Surname</th>
			<th scope="col">Role</th>
			<th scope="col">Hours worked in current month</th>
			<th scope="col">Status</th>
			<th scope="col">Location</th>
			<th scope="col">Edit</th>
			<th scope="col">Delete</th>
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
			<td>${user.userInfo.currentCity.currentCity}</td>
			<td><a href="<c:url value="/editUser/${user.id}"/>">Edit user</a></td>
			<td><a href="<c:url value="/deleteUser/${user.id}"/>">Delete user</a></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('formuser')">Add new user</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visible}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewUser" var = "addUser"/>
	<form:form id = "formuser" action="${addUser}" method="POST" style = "${hidden}">
		<br>
		<label>Login</label>
		<input type="text" name="login" value="${user.login}"/>
		<span class = "error">${error_login}</span>
		<span class = "error">${error_unique}</span>
		<br>
		<label>Password</label>
		<input type="password" name="password"/>
		<span class = "error">${error_realPassword}</span>
		<br>
		<label>Role</label>
		<select name = "role">
			<option value = "admin" ${user.role == 'admin' ? 'selected' : ''}>Administrator</option>
			<option value = "manager" ${user.role == 'manager' ? 'selected' : ''}>Manager</option>
			<option value = "driver" ${user.role == 'driver' ? 'selected' : ''}>Driver</option>
		</select>
		<br>
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
		<select name = "currentCity">
			<c:forEach var="city" items="${cityList}">
				<option value = "${city.currentCity}" ${user.userInfo.currentCity.currentCity == city.currentCity ? 'selected' : ''}>${city.currentCity}</option>
			</c:forEach>
		</select>
		<span class = "error">${error_currentCity}</span>
		<br>
		<input type="submit" value="Add user">
	</form:form>
	<p>Cargo list:</p>
	<table>
		<caption hidden="true">Cargo list</caption>
		<tr>
			<th scope="col">Cargo ID</th>
			<th scope="col">Cargo name</th>
			<th scope="col">Cargo weight(kg)</th>
			<th scope="col">Cargo status</th>
			<th scope="col">Description</th>
		</tr>
		<c:forEach var="cargo" items="${cargoList}">
		<tr>
			<td>${cargo.id}</td>
			<td>${cargo.name}</td>
			<td>${cargo.mass}</td>
			<td>${cargo.status}</td>
			<td>${cargo.description}</td>
		</tr>
		</c:forEach>
	</table>	
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('formcargo')">Add new cargo</button>
	<c:set var = "hiddencargo" value = ""/>
	<c:if test = "${empty visiblecargo}"><c:set var = "hiddencargo" value = "display:none;"/></c:if>
	<c:url value="/addNewCargo" var = "addCargo"/>
	<form:form id = "formcargo" action="${addCargo}" method="POST" style = "${hiddencargo}">
		<br>
		<label>Name</label>
		<input type="text" name="name" value="${cargo.name}"/>
		<br>
		<label>Weight (kg)</label>
		<input type="text" name="mass"/>
		<span class = "error">${error_mass}</span>
		<br>
		<label>Description</label>
		<input type="text" name="description"/>
		<br>
		<input type="submit" value="Add cargo">
	</form:form>
	</div>
</body>
</html>
