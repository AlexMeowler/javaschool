<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Manager page</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<p>Welcome, manager ${current_user_name}!</p>
	<p>Drivers list:</p>
	<table>
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
			<td>${driver.userInfo.currentCity.currentCity}</td>
			<td><a href="<c:url value="/editDriver/${driver.id}"/>">Edit driver</a></td>
			<td><a href="<c:url value="/deleteDriver/${driver.id}"/>">Delete driver</a></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('driverform')">Add new driver</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visibledriver}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewDriver" var = "addDriver"/>
	<form:form id = "driverform" action="${addDriver}" method="POST" style = "${hidden}">
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
		<input type="submit" value="Add driver">
	</form:form>
	<br><br><br>
	<p>Cars list:</p>
	<table>
		<caption hidden="true">Cars list</caption>
		<tr>
			<th scope="col">Registration ID</th>
			<th scope="col">Shift length</th>
			<th scope="col">Capacity(tons)</th>
			<th scope="col">Status</th>
			<th scope="col">Current location</th>
			<th scope="col">Edit</th>
			<th scope="col">Delete</th>
		</tr>
		<c:forEach var="car" items="${carsList}">
		<tr>
			<td>${car.registrationId}</td>
			<td>${car.shiftLength}</td>
			<td>${car.capacityTons}</td>
			<c:set var = "status" value = "Broken"/>
			<c:if test = "${car.isWorking}"><c:set var = "status" value = "Normal"/></c:if>
			<td>${status}</td>
			<td>${car.location}</td>
			<td><a href="<c:url value="/editCar/${car.registrationId}"/>">Edit Car</a></td>
			<td><a href="<c:url value="/deleteCar/${car.registrationId}"/>">Delete Car</a></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('carform')">Add new car</button>
	<c:set var = "hiddencar" value = ""/>
	<c:if test = "${empty visiblecar}"><c:set var = "hiddencar" value = "display:none;"/></c:if>
	<c:url value="/addNewCar" var = "addCar"/>
	<form:form id = "carform" action="${addCar}" method="POST" style = "${hiddencar}">
		<br>
		<label>Registration ID</label>
		<input type="text" name="registrationId" value="${car.registrationId}"/>
		<span class = "error">${error_registrationId}</span>
		<span class = "error">${error_uniqueCarId}</span>
		<br>
		<label>Shift length</label>
		<input type="text" name="shift" value="${car.shiftLength}"/>
		<span class = "error">${error_shiftLength}</span>
		<br>
		<label>Capacity in tons</label>
		<input type="text" name="capacity" value="${car.capacityTons}"/>
		<span class = "error">${error_capacityTons}</span>
		<br>
		<label>Status</label>
		<select name = "isWorking">
			<option value = "true" ${car.isWorking || empty car ? 'selected' : ''}>Normal</option>
			<option value = "false" ${!car.isWorking && not empty car ? 'selected' : ''}>Broken</option>
		</select>
		<br>
		<!-- change to select -->
		<label>Current location</label>
		<input type="text" name="location" value="${car.location}"/>
		<span class = "error">${error_location}</span>
		<br>
		<input type="submit" value="Add car">
	</form:form>
	</div>
</body>
</html>