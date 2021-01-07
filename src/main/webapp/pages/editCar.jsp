<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Edit car</title>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<c:url value="/submitEditedCar" var="submit"/>
	<form:form id = "form" action="${submit}" method="POST">
		<br>
		<label>Car ${car.registrationId}</label>
		<input type="text" name="registrationId" value="${car.registrationId}" hidden="hidden"/>
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
		<span class = "error">${error_isWorking}</span>
		<br>
		<label>Current location</label>
		<select name = "currentCity">
			<c:forEach var="city" items="${cityList}">
				<option value = "${city.currentCity}" ${car.location.currentCity == city.currentCity ? 'selected' : ''}>${city.currentCity}</option>
			</c:forEach>
		</select>
		<span class = "error">${error_location}</span>
		<br>
		<input type="submit" value="Finish editing car">
	</form:form>	
	</div>
</body>
</html>
