<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<% response.setStatus(404); %>
<html>
<head>
    <title>Page not found</title>
    <link href="../Chat/css/bootstrap.min1.css" rel="stylesheet">
    <link href="../Chat/css/style.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid">
    <div class="row body">
        <div class="side col-md-6 col-md-offset-3 col-lg-6 col-lg-offset-3 col-sm-6 col-sm-offset-2 col-xs-8">
            <br>
            <div class="alert alert-warning" role="alert">
                Oops! Page not found (404). You can go
                    <a href="../Chat" class="alert-link">home</a> to Jan Messenger
            </div>

            <p id="error-image-p" ><img src="../Chat/pics/error.jpg" alt="Error: 404"></p>
        </div>
    </div>
</div>

</body>
</html>
