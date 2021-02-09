<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Driver page</title>
	<link rel="shortcut icon" href="<c:url value="static/icon.png"/>" type="image/png">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/main.css/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	Welcome, driver ${user.userInfo.name} ${user.userInfo.surname}!<br>
	You are currently located in <strong>${user.userInfo.city.currentCity}</strong>.<br>
	In this month you worked <strong>${user.userInfo.hoursWorked}</strong> of <strong>176</strong> hours.<br>
	Your status is <strong>&quot;${user.userInfo.status}&quot;</strong>.
	<table>
		<caption hidden="true">Status table</caption>
		<tr hidden="true">
			<th scope="col" hidden="true">Status bar</th>
		</tr>
		<tr>
		<td>
			<c:forEach var="status" items="${statuses}">
				<c:if test = "${user.userInfo.status != status.replace('_', ' ')}">
				<a href="<c:url value="/changeStatus/${status}"/>">Change status to &quot;${status.replace('_', ' ')}&quot;</a>
				</c:if>
			</c:forEach>
		</td>
		</tr>
	</table>
	<c:if test="${not empty error_argument or not empty error_car}">
		<span class="error">${error_argument}${error_car}</span><br>
	</c:if>
	Your ID is ${user.id}.<br>
	<c:set var = "orderText" value = "No orders assigned"/>
	<c:if test = "${not empty order}"><c:set var = "orderText" value = "Currently assigned order:"/></c:if>
	${orderText}
	<c:if test = "${not empty order}">
	<table class ="info-table">
		<caption hidden="true">Order info</caption>
		<tr>
			<th scope="col">Order ID</th>
			<th scope="col">Status</th>
			<th scope="col">Assigned car</th>
			<th scope="col">Route</th>
			<th scope="col">Assigned cargo</th>
		</tr>
		<tr>
			<td>${order.id}</td>
			<c:set var = "status" value = "Not completed"/>
			<c:if test = "${order.isCompleted}"><c:set var = "status" value = "Completed"/></c:if>
			<td>${status}</td>
			<td>
				<c:if test="${not empty order.car}">${order.car.registrationId}</c:if>
				<c:if test="${empty order.car}">-</c:if>
			</td>
			<td>
				<c:forEach var="route" items="${routeList}" varStatus="i">
					<c:if test="${routeCounter eq i.index}">
						<strong>${route}<br></strong>
					</c:if>
					<c:if test="${routeCounter ne i.index}">
						${route}<br>
					</c:if>
				</c:forEach>
			</td>
			<td>
				<c:forEach var="cargo" items="${order.cargo}">
					<c:set var = "iter" value = "${cargo.points.iterator()}"/>
					<c:set var = "pointA" value = "${iter.next()}"/>
					<c:set var = "pointB" value = "${iter.next()}"/>
					<c:if test="${pointA.isLoading}">
						<c:set var = "cityFrom" value = "${pointA.city.currentCity}"/>
						<c:set var = "cityTo" value = "${pointB.city.currentCity}"/>
					</c:if>
					<c:if test="${pointB.isLoading}">
						<c:set var = "cityFrom" value = "${pointB.city.currentCity}"/>
						<c:set var = "cityTo" value = "${pointA.city.currentCity}"/>
					</c:if>
					${cargo.id}: ${cargo.name} (To deliver from <strong>${cityFrom}</strong> to <strong>${cityTo}</strong>)<br>
				</c:forEach>
			</td>
		</tr>
	</table>
	<p>Your co-drivers for this order:<br>
	<c:set var = "otherDrivers" value = "false"/>
	<c:if test="${not empty order.driverInfo}">
		<c:forEach var="driverInfo" items="${order.driverInfo}">
			<c:if test = "${driverInfo.user.id ne user.id}">
				<c:set var = "otherDrivers" value = "true"/>
				${driverInfo.name} ${driverInfo.surname} (${driverInfo.user.id})<br>
			</c:if>
		</c:forEach>
	</c:if>
	<c:if test="${not otherDrivers}">
		No other drivers were assigned.
	</c:if>
	</p>
	<c:if test="${empty user.userInfo.hoursDrived or user.userInfo.hoursDrived + nextHopLength <= order.car.shiftLength}">
		<c:if test="${not empty nextHop}">
			<p>Next city on your route is <strong>${nextHop}</strong>, it should take about <strong>${nextHopLength}</strong> hours to reach it. 
			<a href="<c:url value="/changeLocation"/>">Change location</a></p>
		</c:if>
		<c:if test="${empty nextHop}">
			<p>No next route point available.</p>
		</c:if>
	</c:if>
	<c:if test="${not empty user.userInfo.hoursDrived and user.userInfo.hoursDrived + nextHopLength > order.car.shiftLength}">
		<p>Car shift length limit exceeded, you can't drive any further. You can end shift now, but remember, after that you will be
		<strong>unassigned from the order</strong>.<br>Please, <strong>do not end shift before you unload cargo</strong> which destination point is your current location.
		</p>
	</c:if>
	<c:if test="${not empty error_city}">
		<span class="error">${error_city}</span><br>
	</c:if>
	<c:if test="${not empty error_cityNotMatching}">
		<span class = "error">${error_cityNotMatching}</span><br>
	</c:if>
	<c:if test="${not empty error_globalCargo}">
		<span class = "error">${error_globalCargo}</span><br>
	</c:if>
	<c:if test="${not empty error_cargoManagement}">
		<span class="error">${error_cargoManagement}</span><br>
	</c:if>
	<p>Cargo management menu:<br>Please, remember: changes to cargo status <strong>can't be undone</strong></p>
	<table class="info-table">
		<caption hidden="true">Cargo menu</caption>
		<tr>
			<th scope="col">ID &amp; Name</th>
			<th scope="col">Description</th>
			<th scope="col">Status</th>
			<th scope="col">Action</th>
		</tr>
		<c:forEach var="cargo" items="${order.cargo}">
			<tr>
				<td>${cargo.id}: ${cargo.name}</td>
				<td>${cargo.description}</td>
				<td>${cargo.status}</td>
				<td>
					<c:set var = "hrefText" value = "${null}"/>
					<c:if test="${cargo.status eq 'prepared'}">
						<c:set var = "hrefText" value = "Load"/>	
					</c:if>
					<c:if test="${cargo.status eq 'loaded'}">
						<c:set var = "hrefText" value = "Unload"/>	
					</c:if>
					<c:if test="${not empty hrefText}">
						<a href="<c:url value="/updateCargo/${cargo.id}"/>">${hrefText}</a>
					</c:if>
				</td>
			</tr>
		</c:forEach>
	</table>
	</c:if>
	<div class="footer"></div>
	</div>
</body>
</html>