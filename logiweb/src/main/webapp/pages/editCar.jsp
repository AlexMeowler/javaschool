<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="en">
<head>
	<title>Edit car</title>
	<link rel="shortcut icon" href="<c:url value="static/icon.png"/>" type="image/png">
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
		<table class="form-table">
		  <caption hidden="true">form</caption>
            <tr hidden="true"><th scope="col">Hidden header</th></tr>
			<tr>
				<td><label>Car ${car.registrationId}</label></td>
				<td><input type="text" name="registrationId" value="${car.registrationId}" hidden="hidden"/></td>
				<td><span class = "error">${error_carUnavailable}</span></td>
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
		<input type="submit" value="Finish editing car">
	</form:form>
	<div class="footer"></div>	
	</div>
</body>
</html>
