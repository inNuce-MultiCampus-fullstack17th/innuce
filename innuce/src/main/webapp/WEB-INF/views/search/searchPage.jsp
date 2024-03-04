<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<jsp:include page="/WEB-INF/views/header/head.jsp"/>

<!-- css -->
<link rel="stylesheet" type="text/css" href="/css/searchPage.css">
<link rel="stylesheet" type="text/css" href="/css/header.css">
<link rel="stylesheet" type="text/css" href="/css/main_top.css">
<link rel="stylesheet" type="text/css" href="/css/mypage.css">


<script defer src="/js/searchPage.js"></script>



</head>
<body>
<!-- HEADER -->
<header>

<%@ include file ="/WEB-INF/views/header/header.jsp" %>


</header>

<nav>
<%@ include file ="/WEB-INF/views/nav/nav.jsp" %>
</nav>

<!-- MAIN -->
<main>

<!-- newsList -->
<%@ include file ="/WEB-INF/views/search/content.jsp" %>
</main>

 <!--TO TOP BUTTON-->
 <div id="to-top">
   <div class="material-icons">arrow_upward</div>
 </div>
<script defer src="/js/search-option.js"></script>
</body>
</html>