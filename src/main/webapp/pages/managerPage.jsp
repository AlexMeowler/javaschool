<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
	<link rel="stylesheet" type="text/css" href="<c:url value="static/bootstrap.min.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/menu.css"/>">
	<link rel="stylesheet" type="text/css" href="<c:url value="static/main.css"/>">
	<script type="text/javascript" src = "<c:url value="static/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<div class="container main-body">
	<p>Welcome, manager ${current_user_name}!</p>
	<p>Driver list:</p>
	<table>
		<tr>
			<td>ID</td>
			<td>Login</td>
			<td>Name</td>
			<td>Surname</td>
			<td>Delete</td>
		</tr>
		<c:forEach var="driver" items="${driverList}">
		<tr>
			<td>${driver.id}</td>
			<td>${driver.login}</td>
			<td>${driver.userInfo.name}</td>
			<td>${driver.userInfo.surname}</td>
			<td><a href="${pageContext.request.contextPath}/deleteDriver/${driver.id}">Delete driver</a></td>
		</tr>
		</c:forEach>
	</table>
	<button class = "table-edit-button" name = "openOrCloseForm" onclick = "showForm()">Add new driver</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visible}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewDriver" var = "addDriver"/>
	<form:form id = "form" action="${addDriver}" method="POST" style = "${hidden}">
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
		<input type="submit" value="Add driver">
	</form:form>
	<p>Cars list:</p>
	<table>
		<tr>
			<td>Registration ID</td>
			<td>Shift length</td>
			<td>Capacity(tons)</td>
			<td>Status</td>
			<td>Current location</td>
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
		</tr>
		</c:forEach>
	</table>
	</div>
</body>
</html>