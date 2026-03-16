<%--
  Redirect root requests to Angular app residing under /src/
  Allows accessing http://localhost:8080/ to reach the login page.
--%>
<%
    response.sendRedirect(request.getContextPath() + "/src/index.html");
%>