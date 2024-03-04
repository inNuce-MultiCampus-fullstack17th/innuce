<%@page import="com.mc.innuce.domain.user.dto.UserDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<jsp:include page="/WEB-INF/views/header/head.jsp" />

<link rel="stylesheet" type="text/css" href="/css/header.css">
<link rel="stylesheet" type="text/css" href="/css/mypage.css">
<link rel="stylesheet" type="text/css" href="/css/debate.css">
<script defer src="/js/main.js"></script>
<script
	src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

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
		}// else
		})// on
	
</script>
	<!--  header -->
	<header>
		<div class="logo-txt-cover">
			<%@ include file="/WEB-INF/views/header/topBar.jsp"%>
			<!--  좌측 네비바 -->
			<div id="myPage_navigater">
				<div class='title'>마이페이지</div>
				<button id="info_change" clicked="none">
					<span class="material-symbols-outlined">manage_accounts</span>&nbsp;회원
					정보 수정
				</button>
				<button id="my_scrap" clicked="none">
					<i class='fa-solid fa-bookmark'></i>&nbsp;스크랩한 기사
				</button>
				<button id="my_chatting" clicked="yes">
					<i class="fa-regular fa-comments"></i>&nbsp;참여중인 채팅방
				</button>
				<button id="delete" clicked="none">회원 탈퇴</button>
			</div>

			<%@ include file="/WEB-INF/views/header/chattingroomlist.jsp"%>
		</div>
		<%@ include file="/WEB-INF/views/header/logo.jsp"%>

	</header>


	<!--  마이페이지 내용 -->
	<main>

		<div id="myPage_main"></div>

		<div class="total-container">
			<div class="room-container">
				<div class="inner">
					<c:if test="${totalCount != 0}">
						<c:forEach items="${openDebateRoomList}" varStatus="room">
							<div class='content'>

								<a class="img-cover"
									href="/debate/${room.current.debate_room_key }"> <i
									class="fa-regular fa-comments"></i>
								</a> <a class='a' href="/debate/${room.current.debate_room_key }">
									<p id='${room.count }-1' class='room-name'>${room.current.debate_room_name }</p>
									<p id='${room.count }-2' class='room-description'>실시간 참여자 수
										: ${openDebateRoomUserConnectCountList[room.index]}</p>
									<p id='${room.count }-3' class='room-description'>전체 참여자 수
										: ${openDebateRoomUserCountList[room.index]}</p>
									<p id='${room.count }-3' class='room-description'>생성일자 :
										${room.current.debate_room_regdate}</p> <c:choose>
										<c:when test="${room.current.debate_room_status == 2 }">
											<p id='${room.count }-5' class='room-description'>열림</p>
										</c:when>
										<c:when test="${room.current.debate_room_status == 1 }">
											<p id='${room.count }-5' class='room-description'>10분 뒤
												닫힘</p>
										</c:when>
										<c:when test="${room.current.debate_room_status == 0 }">
											<p id='${room.count }-5' class='room-description'>닫힘</p>
										</c:when>
									</c:choose>
								</a>

							</div>
						</c:forEach>
					</c:if>
					<c:if test="${totalCount == 0}">
						<div class='content'>
							<p>채팅방이 존재하지 않습니다.</p>
						</div>
					</c:if>
				</div>

				<div class="paging">
					<%
					int pageCount = (Integer) request.getAttribute("pageCount");
					int totalCount = (Integer) request.getAttribute("totalCount");

					int totalPage = 0;
					if (totalCount % pageCount == 0) {
						totalPage = totalCount / pageCount;
					} else {
						totalPage = totalCount / pageCount + 1;
					}

					for (int i = 1; i <= totalPage; i++) {
					%>

					<a href="/mypageChatting?page=<%=i%>"><%=i%></a>&nbsp;
					<%
					}
					%>

				</div>

			</div>
		</div>

	</main>
</body>
</html>