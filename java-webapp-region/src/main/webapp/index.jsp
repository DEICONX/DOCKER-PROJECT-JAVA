<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
    <title>Motivator Region App</title>
</head>
<body>
    <h1>Welcome to Motivator Region App</h1>
    <form action="motivator" method="post">
        <label for="region">Enter Region (e.g., Asia/Kolkata):</label>
        <input type="text" name="region" id="region" required>
        <input type="submit" value="Show Time & Quote">
    </form>
</body>
</html>
