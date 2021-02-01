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
	<!--<c:url value="/addCityInfo" var = "addCityInfo"/>
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
	<br> -->
	<c:url value="/testJMS" var = "testJMS"/>
	<form:form action="${testJMS}" method="POST">
		<input type="submit" value="Add cars information to database">
	</form:form>
	<span class = "error">${error_userDeletionFailed}</span>
	<p>Here is user list:</p>
	<table class="info-table">
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
			<td>${user.userInfo.city.currentCity}</td>
			<td><a href="<c:url value="/editUser/${user.id}"/>">Edit user</a></td>
			<td><a href="<c:url value="/deleteUser/${user.id}"/>">Delete user</a></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('formuser')">Add new user</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visible}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<br><span class = "error">${error_userMaliciousInput}</span>
	<c:url value="/addNewUser" var = "addUser"/>
	<form:form id = "formuser" action="${addUser}" method="POST" style = "${hidden}">
		<br>
		<table class="form-table">
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
			<td><label>Role</label></td>
			<td><select name = "role">
					<option value = "admin" ${user.role == 'admin' ? 'selected' : ''}>Administrator</option>
					<option value = "manager" ${user.role == 'manager' ? 'selected' : ''}>Manager</option>
					<option value = "driver" ${user.role == 'driver' ? 'selected' : ''}>Driver</option>
				</select>
			</td>
		</tr>
		<tr>
			<td><label>Name</label></td>
			<td><input type="text" name="name" value="${user.userInfo.name}"/></td>
			<td><span class = "error">${error_name} ${error_noDigitsName}</span></td>
		</tr>
		<tr>
			<td><label>Surname</label></td>
			<td><input type="text" name="surname" value="${user.userInfo.surname}"/></td>
			<td><span class = "error">${error_surname} ${error_noDigitsSurname}</span></td>
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
		<input type="submit" value="Add user">
	</form:form>
	<p>Cargo list:</p>
	<table class="info-table">
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
	<br><span class = "error">${error_cargoMaliciousInput}</span>
	<form:form id = "formcargo" action="${addCargo}" method="POST" style = "${hiddencargo}">
		<br>
		<table class="form-table">
		<tr>
			<td><label>Name</label></td>
			<td><input type="text" name="name" value="${cargo.name}"/></td>
			<td><span class = "error">${error_name}</span></td>
		</tr>
		<tr>
			<td><label>Weight (kg)</label></td>
			<td><input type="text" name="mass"/></td>
			<td><span class = "error">${error_mass}</span></td>
		</tr>
		<tr>
			<td><label>Description</label></td>
			<td><input type="text" name="description"/></td>
			<td><span class = "error">${error_description}</span></td>
		</tr>
		</table>
		<input type="submit" value="Add cargo">
	</form:form>
	<div class="footer"></div>
	</div>
</body>
</html>
