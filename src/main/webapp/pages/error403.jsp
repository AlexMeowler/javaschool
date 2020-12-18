<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<body>
	<h2>Access Denied</h2>
	<c:if test="${not empty username}">
	<p>Sorry, <c:out value="${username}"/>, but you can't access this page...</p>
	</c:if>
	<a href="/logiweb/home">Return to home page</a>
</body>
</html>