<%@page import="com.mc.innuce.domain.user.dto.UserDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<jsp:include page="/WEB-INF/views/header/head.jsp"/>

<link rel="stylesheet" type="text/css" href="/css/header.css">
<link rel="stylesheet" type="text/css" href="/css/mypage.css">

<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script defer src="/js/main.js"></script>
<style>

</style>
</head>
<body>
<script>
	$(document).ready(function(){
		
	
		// 세션에 유저가 없어서 마이페이지를 보여주면 안되는경우
		if(${empty sessionScope.login_user})
		{
			alert("세션이 만료되었습니다. 메인화면으로 이동합니다.");
		    location.href = "main";
		}
		else{
			
			// 회원 정보 수정 눌렀을때
			 $("#info_change").on('click',function(){
				 location.href = "mypageChangeinfo"
			})//info_change_click
			
			// 스크랩한 기사 눌렀을때
			 $("#my_scrap").on('click',function(){
				 location.href = "mypageScrap"	
			 })// my_scrap click
			 
			// 참여중인 채팅방 버튼 눌렀을때
			$("#my_chatting").on('click',function(){
				location.href = "mypageChatting"
			})// my_chatting click
			
			// 회원탈퇴 버튼 눌렀을때
			$("#delete").on('click',function(){
				location.href = "mypageDelete"
			})// delete click
			
			// 탈퇴하기 버튼 눌렀을때
			$("#deleteButton").on('click',function(){
				$.ajax({ 
					url : "deleteuser",
					type : 'post',
					success : function(response){
						alert("회원탈퇴가 완료됐습니다.")
						location.href = "main"
						
						}//success
					});//ajax
				})//delete button click
			
		}// else
	})// on
	
</script>
<!--  header -->
<header>
	<div class="logo-txt-cover">
	<%@ include file ="/WEB-INF/views/header/topBar.jsp" %>
		<!--  좌측 네비바 -->
	<div id = "myPage_navigater" >
		<div class='title'>마이페이지</div>
		<button id="info_change" clicked="none"><span class="material-symbols-outlined">manage_accounts</span>&nbsp;회원 정보 수정</button>
		<button id="my_scrap" clicked="none"><i class='fa-solid fa-bookmark'></i>&nbsp;스크랩한 기사 </button>
		<button id="my_chatting" clicked="none"><i class="fa-regular fa-comments"></i>&nbsp;참여중인 채팅방</button>
		<button id="delete" clicked="yes"> 회원 탈퇴</button>
	</div>
	
	<%@ include file="/WEB-INF/views/header/chattingroomlist.jsp"%>
  </div>
  <%@ include file ="/WEB-INF/views/header/logo.jsp" %>

</header>

<!--  마이페이지 내용 -->
<main>

	<div id ="myPage_main">
		<div class="des"> 정말로 회원을 탈퇴하시겠습니까? </div><br>
		<div class="des"> 회원 탈퇴 후 2분내로 로그인 하시면 복구하실 수 있습니다.</div><br>
		<input class="des" id= 'deleteButton' type='button' value='탈퇴하기' >
	</div>

</main>
</body>
</html>