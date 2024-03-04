<%@page import="com.mc.innuce.domain.user.dto.UserDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>마이페이지</title>

<jsp:include page="/WEB-INF/views/header/head.jsp" />

<link rel="stylesheet" type="text/css" href="/css/header.css">
<link rel="stylesheet" type="text/css" href="/css/main.css">
<link rel="stylesheet" type="text/css" href="/css/mypage.css">
<script defer src="/js/main.js"></script>
<script
	src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<style>
</style>
</head>
<body>
	<script>
	if(${empty sessionScope.login_user})
	{
		alert("세션이 만료되었습니다. 메인화면으로 이동합니다.");
    	location.href = "main";
	}
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
			
			let user_id = '${sessionScope.login_user.user_id}'
			$("i").addClass("checked")
			// 북마크 버튼 눌렀을떄 (스크랩 취소)
			$('i').on('click',function(){
				
				$.ajax({
					type : 'post',
					url : 'scrapnewscancel',
					data : {'user_id' : user_id, 'news_key' : $(this).attr('news')},
					success : function(response){
						alert("스크랩이 취소 됐습니다")
						location.reload(true)
					}//success
				})//ajax
			})//main i click
			
			// 검색 버튼 눌렀을때
			$("#search_button").on('click',function(){
				// 제목기준 검색
				if($('#search_option option:selected').val() == "search_title"){
					location.href = "/mypageScrap?search_title="+$('#search').val()
				}
				// 내용기준 검색
				else{
					location.href = "/mypageScrap?search_content="+$('#search').val()
				}
					
			})// search_button click
	
		
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
				<button id="my_scrap" clicked="yes">
					<i class='fa-solid fa-bookmark'></i>&nbsp;스크랩한 기사
				</button>
				<button id="my_chatting" clicked="none">
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
	<div id="myPage_main">
			<div id="mypage_scrap_main">
			<c:choose>
			<c:when test= "${scrapList.size() ==0}">
			<div class="news-cover">
			<div class="news-content">
			<div id='search_board_box'>
				<select id='search_option'>
					<option value='search_title'>제목</option>
					<option value='search_content'>내용</option>
				</select> <input type='text' , placeholder='검색어입력' , id='search'>
				<button id='search_button' type='button'>검색</button>
			</div>		
			</div>
			</div>
			<div> 스크랩된 기사가 없습니다 </div>
			</c:when> 
			<c:otherwise>

				<div class="news-cover">
					<div class="news-content">
						<div class="cover">
							<div id="tab-1-keyword" class="tab-content">
								<c:forEach var="newsDTO" items="${scrapList}" varStatus="status">

									<div class='content' value="${newsDTO.news_key }">
										<a class="img-cover" href="news?newsKey=${newsDTO.news_key }">
											<img id="img-1" alt="images"
											src="${newsDTO.news_thumbnailuri2 }"
											onerror="this.src='/images/inNUCE_logo.png'" />
										</a> <a class='a' href="news?newsKey=${newsDTO.news_key }">
											<div id='${status.index }-1' class='date'>${newsDTO.news_writendate }</div>
											<div id='${status.index }-2' class='main'>${newsDTO.news_title }</div>
											<div id='${status.index }-3' class='cont'>${newsDTO.summ_content }<br />
											</div>
										</a>

										<div class='bookmark-cover'>
											<i class='fa-solid fa-bookmark' news='${newsDTO.news_key}'></i>
										</div>
									</div>
								</c:forEach>
							</div>
						</div>

						<div class="paging">
							<%
							int pageCount = (Integer) request.getAttribute("pageCount");
							String search_title = (String) request.getAttribute("search_title");
							String search_content = (String) request.getAttribute("search_content");
							for (int i = 1; i <= pageCount; i++) {
							%>

							<a
								href="/mypageScrap?pageNum=<%=i%>&search_title=<%=search_title%>&search_content=<%=search_content%>"><%=i%></a>&nbsp;
							<%
							}
							%>
						</div>
						<div id='search_board_box'>
							<select id='search_option'>
								<option value='search_title'>제목</option>
								<option value='search_content'>내용</option>
							</select> <input type='text' , placeholder='검색어입력' , id='search'>
							<button id='search_button' type='button'>검색</button>
						</div>
					</div>
				</div>
				</c:otherwise>
				</c:choose>
			</div>
		</div>
	</main>
</body>
</html>