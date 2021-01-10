<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Driver page</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/main.css/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	Welcome, driver ${user.userInfo.name} ${user.userInfo.surname}!<br>
	You are currently located in ${user.userInfo.currentCity.currentCity}.<br>
	Your status is &quot;${user.userInfo.status}&quot;.
	<table>
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
	Your ID is ${user.id}.
	<c:set var = "orderText" value = "No orders assigned"/>
	<c:if test = "${not empty order}"><c:set var = "orderText" value = "Currently assigned order:"/></c:if>
	${orderText}
	<c:if test = "${not empty order}">
	<table>
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
			<td>${order.car.registrationId}</td>
			<td><c:forEach var="route" items="${routeList}"><br></c:forEach></td>
			<td><c:forEach var="cargo" items="${order.cargo}">${cargo.id}: ${cargo.name} (${cargo.description})<br></c:forEach></td>
		</tr>
	</table>
	<p>Your co-drivers for this order:</p>
	<c:forEach var="driver" items="${order.drivers}">
		<c:if test = "${driver.id != user.id}">${driver.userInfo.name} ${driver.userInfo.surname} (${driver.id})<br></c:if>
	</c:forEach>
	</c:if>
	</div>
</body>
</html>