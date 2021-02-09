<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Manager page (cars editing)</title>
	<link rel="shortcut icon" href="<c:url value="/static/icon.png"/>" type="image/png">
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="/static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="/static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<p>Welcome, manager ${current_user_name}!</p>
	<span class = "error">${error_carDeletionFailed}</span>
	<p>Cars list:</p>
	<table class="info-table">
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
			<td>${car.location.currentCity}</td>
			<td><a href="<c:url value="/editCar/${car.registrationId}"/>">Edit Car</a></td>
			<td><a href="<c:url value="/deleteCar/${car.registrationId}"/>">Delete Car</a></td>
		</tr>
		</c:forEach>
		<tr>
			<td style="visibility:hidden; border-bottom-color: rgba(0, 0, 0, 0.0)" colspan="7">Empty row</td>
		</tr>
		<tr>
			<td style="border-color: rgba(0, 0, 0, 0.0)"><input type="submit" value="Previous page" onclick="window.location='<c:url value="/managerCarsPage/${page - 1}"/>';" /></td>
			<td colspan="5" style="border-color: rgba(0, 0, 0, 0.0)"></td>
			<td style="border-color: rgba(0, 0, 0, 0.0)"><input type="submit" value="Next page" onclick="window.location='<c:url value="/managerCarsPage/${page + 1}"/>';" /></td>
		</tr>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('carform')">Add new car</button>
	<c:set var = "hiddencar" value = ""/>
	<c:if test = "${empty visiblecar}"><c:set var = "hiddencar" value = "display:none;"/></c:if>
	<c:url value="/addNewCar" var = "addCar"/>
	<form:form id = "carform" action="${addCar}" method="POST" style = "${hiddencar}">
		<br>
		<table class="form-table">
		  <caption hidden="true">form</caption>
            <tr hidden="true"><th scope="col">Hidden header</th></tr>
			<tr>
				<td><label>Registration ID</label></td>
				<td><input type="text" name="registrationId" value="${car.registrationId}"/></td>
				<td>
					<span class = "error">${error_registrationId}</span>
					<span class = "error">${error_uniqueCarId}</span>
				</td>
			</tr>
			<tr>
				<td><label>Shift length</label></td>
				<td><input type="text" name="shift" value="${car.shiftLength}"/></td>
				<td><span class = "error">${error_shiftLength}</span></td>
			</tr>
			<tr>
				<td><label>Capacity in tons</label></td>
				<td><input type="text" name="capacity" value="${car.capacityTons}"/></td>
				<td><span class = "error">${error_capacityTons}</span></td>
			</tr>
			<tr>
				<td><label>Status</label></td>
				<td><select name = "isWorking">
						<option value = "true" ${car.isWorking || empty car ? 'selected' : ''}>Normal</option>
						<option value = "false" ${!car.isWorking && not empty car ? 'selected' : ''}>Broken</option>
					</select>
				</td>
				<td><span class = "error">${error_isWorking}</span></td>
			</tr>
			<tr>
				<td><label>Current location</label></td>
				<td><select name = "currentCity">
						<c:forEach var="city" items="${cityList}">
							<option value = "${city.currentCity}" ${car.location.currentCity == city.currentCity ? 'selected' : ''}>${city.currentCity}</option>
						</c:forEach>
					</select>
				</td>
				<td><span class = "error">${error_location}</span></td>
			</tr>
		</table>
		<input type="submit" value="Add car">
	</form:form>
	<div class="footer"></div>
	</div>
</body>
</html>