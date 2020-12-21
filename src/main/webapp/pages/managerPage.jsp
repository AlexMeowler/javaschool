<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
	<script type="text/javascript" src = "<c:url value="js/my_js_library.js"/>"></script>
</head>
<body>
	<jsp:include page="menu.jsp"/>
	<p>Welcome, manager ${name}!</p>
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
	<button name = "openOrCloseForm" onclick = "showForm()">Add new user</button>
	<c:set var = "hidden" value = ""/>
	<c:if test = "${empty visible}"><c:set var = "hidden" value = "display:none;"/></c:if>
	<c:url value="/addNewDriver" var = "addDriver"/>
	<form:form id = "form" action="${addDriver}" method="POST" style = "${hidden}">
		<label>Login</label>
		<input type="text" name="login"/>
		<label>Password</label>
		<input type="password" name="password"/>
		<br>
		<label>Name</label>
		<input type="text" name="name"/>
		<label>Surname</label>
		<input type="text" name="surname"/>
		<br>
		<input type="submit" value="Add user">
	</form:form>
</body>
</html>