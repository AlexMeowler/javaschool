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
	</div>
</body>
</html>