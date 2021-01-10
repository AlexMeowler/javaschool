<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Cargo and orders managing</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<c:if test = "${empty counter_value}"><c:set var = "counter_value" value = "0"/></c:if>
	<script>var counter = ${counter_value}</script>
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<p>Welcome, manager ${current_user_name}!</p>
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
	<p>Orders list:</p>
	<table>
		<caption hidden="true">Orders list</caption>
		<tr>
			<th scope="col">Order ID</th>
			<th scope="col">Status</th>
			<th scope="col">Assigned car</th>
			<th scope="col">Route</th>
			<th scope="col">Assigned drivers</th>
			<th scope="col">Assigned cargo</th>
		</tr>
		<c:forEach var="order" items="${ordersList}">
		<tr>
			<td>${order.id}</td>
			<c:set var = "status" value = "Not completed"/>
			<c:if test = "${order.isCompleted}"><c:set var = "status" value = "Completed"/></c:if>
			<td>${status}</td>
			<td>${order.car.registrationId}</td>
			<td>${order.route}</td>
			<td><c:forEach var="driver" items="${order.drivers}">${driver.userInfo.name} ${driver.userInfo.surname} (${driver.id})<br></c:forEach></td>
			<td><c:forEach var="cargo" items="${order.cargo}">${cargo.id}: ${cargo.name}<br></c:forEach></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm('formorder')">Add new order</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visible}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewOrder" var = "addOrder"/>
	<form:form id = "formorder" action="${addOrder}" method="POST" style = "${hidden}">
		<span class = "error">${error_globalCity} ${error_globalCargo}</span>
		<br>
		<span class = "error">${error_emptyInput} ${error_globalCar} ${error_globalDrivers}</span>
		<br>
		<div id="rows">
			<c:forEach var="routePoint" items="${routePoints}" varStatus = "i">
				<div id="div${i.index}">
					<label>City</label>
					<select id="city${i.index}" name="list[${i.index}].cityName">
						<c:forEach var="city" items="${cityList}">
							<option value = "${city.currentCity}" ${routePoint.cityName == city.currentCity ? 'selected' : ''}>${city.currentCity}</option>
						</c:forEach>
					</select>
					<label>Cargo</label>
					<select id="cargo${i.index}" name="list[${i.index}].cargoId">
						<c:forEach var="cargo" items="${cargoList}">
							<option value = "${cargo.id}" ${routePoint.cargoId == cargo.id ? 'selected' : ''}>${cargo.id}: ${cargo.name}</option>
						</c:forEach>
					</select>
					<label>Status</label>
					<select id="status${i.index}" name="list[${i.index}].isLoading">
						<option value="true" ${routePoint.isLoading ? 'selected' : ''}>Load</option>
						<option value="false" ${!routePoint.isLoading ? 'selected' : ''}>Drop</option>
					</select>
					<a id="a${i.index}" href="javascript: deleteRow(${i.index});">Delete this row</a>
					<span class = "error">${routePoint.error}</span>
				</div>
			</c:forEach>
		</div>
		<button type="button" class = "table-edit-button" name = "addRoutePoint" onclick = "addRow()">Add route point</button>
		<br>
		<br>
		<input type="submit" value="Register order">
	</form:form>
	</div>
</body>
</html>