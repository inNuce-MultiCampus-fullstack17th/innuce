<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<jsp:include page="/WEB-INF/views/header/head.jsp" />

<!-- css -->
<link rel="stylesheet" type="text/css" href="/css/header.css">
<link rel="stylesheet" type="text/css" href="/css/nav.css">
<link rel="stylesheet" type="text/css" href="/css/news_main.css">

<script defer src="/js/news_main.js"></script>
<script defer src="/js/searchPage.js"></script>

</head>
<body>
	<!-- HEADER -->
	<header>

		<%@ include file="/WEB-INF/views/header/header.jsp"%>


	</header>

	<nav>
		<%@ include file="/WEB-INF/views/nav/nav.jsp"%>
	</nav>

	<!-- MAIN -->
	<main>

		<div class="news-cover">
			<div class="news-content">
				<div class="cover">

					<div class="title">
						<p>${News.news_title }</p>
					</div>

					<div class="image">
						<img alt="이미지" src="${News.news_thumbnailuri2 }" onerror="this.src='/images/inNUCE_logo.png'"/>
					</div>
					<!-- 탭버튼 -->
					<div class="sub-menu" id="change">
						<ul class='menu'>
							<!-- <li class="tab-link current" data-tab="tab-1">짧은 요약</li> -->
							<li class="tab-link current" data-tab="tab-1">기사 요약</li>
							<li class="tab-link" data-tab="tab-2">기사 전문</li>
							<div class='bookmark-cover'>
	     					<i id='scrap_button' class='fa-solid fa-bookmark' news='${News.news_key}'></i>
     					</div>
						</ul>
					
						
						<div id="tab-1" class="tab-content current">
	
							<div class="content">
								<p>${News.summ_content }</p>
							</div>
	
						</div>
	
						<div id="tab-2" class="tab-content">
	
							<div class="content">
								<p>${News.news_content }</p>
							</div>
	
						</div>
	
					</div>
					
					
					<div class="end">
						<a class="original" href="${News.news_originuri }">
							<div>기사 원문</div>
						</a>
					</div>

				</div>
			</div>
		</div>

	</main>
<jsp:include page="/WEB-INF/views/search/scrapInNews.jsp" />
 <!--TO TOP BUTTON-->
 <div id="to-top">
   <div class="material-icons">arrow_upward</div>
 </div>

</body>
</html>

