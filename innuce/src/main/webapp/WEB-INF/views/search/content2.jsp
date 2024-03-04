s<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<script>
window.onload = function(){
	
	
	// 세션에 로그인된 유저가 없으면 스크랩 기능을 막기 위한 코드
	if(${empty sessionScope.login_user} ){
	
		$('main i').on('click',function(){
			
			var user_will_login = window.confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")
			// 로그인 하겠다고 하는 경우
			if(user_will_login == true){
				location.href ='/login'
			}
			// 로그인 안한다고 하는경우 (그냥 confirm창만 닫아주면 됨.)
			else{}
			
		})//on
	}//if
	// 세션에 로그인 된 유저 있으면 스크랩 기능을 준다
	else{
		
		let user_id = '${sessionScope.login_user.user_id}'
		
		// 해당 유저가 스크랩한 기사리스트
		let scrap_list=[];
		//우선 ajax로 해당 유저가 스크랩한 기사들의 news_key를 ajax로 받아온다
		$.ajax({
			type : 'post',
			url : 'getscraplist',
			data : {'user_id' : user_id},
			success : function(response){
				
				for (var i = 0 ; i < response.length;i++){
					scrap_list.push(response[i])
				}//for
					
		
		
		// 유저가 스크랩한 기사인경우 북마크바 속성 클릭된거로 바꿔주기
		for(var i = 0; i < $('.bookmark-cover').find('i').length; i++){
			
			if(scrap_list.includes(Number($("[scrapNum]")[i].getAttribute('news')))){
				$("[scrapNum]")[i].setAttribute("class",'fa-solid fa-bookmark checked')
				
			}
			else{
				$("[scrapNum]")[i].setAttribute("class",'fa-solid fa-bookmark')
				
			}//else
		}//for
		
		
		$('main i').on('click',function(){
			// 이미 스크랩 된 기사인경우
			if($(this).attr("class")=='fa-solid fa-bookmark checked'){
				
				// 북바크 색상 변경
				$(this).removeClass("checked")
				// 북마크 했다는 사실 서버/db에 알리기
				$.ajax({
					type : 'post',
					url : 'scrapnewscancel',
					data : {'user_id' : user_id, 'news_key' : $(this).attr('news')},
					success : function(response){
						alert("스크랩이 취소 됐습니다")
						location.reload(true)
					}//success
				})//ajax
			}
			// 아닌경우
			else{
				
				// 북바크 색상 변경
				$(this).addClass("checked")
			
				// 북마크 했다는 사실 서버/db에 알리기
				$.ajax({
					type : 'post',
					url : 'scrapnews',
					data : {'user_id' : user_id, 'news_key' : $(this).attr('news')},
					success : function(response){
						alert("스크랩이 완료 됐습니다")
						location.reload(true)
					}//success
				})//ajax
			}//else
		})// on
		}//success
	})//ajax
	}//else

}//onload
</script>

<div class="news-cover">


<div class="news-cover">
	<div class="news-content">
		<div class="cover">

			<div class="main-title">
				<p>${keyword }</p>
				<p>${noneKeyword }</p>
			</div>
	
			<div id="tab-1-keyword" class="tab-content">
			
			<c:forEach var="newsDTO" items="${newsList }" varStatus="status">
			
      	<div class='content' value="${newsDTO.news_key }">
					<a class="img-cover" href="news?newsKey=${newsDTO.news_key }">
						<img id="img-1" alt="images" src="${newsDTO.news_thumbnailuri2 }" onerror="this.src='/images/inNUCE_logo.png'"/>
					</a>
					
      		<a class='a' href="news?newsKey=${newsDTO.news_key }">
      			<div id='${status.index }-1' class='date'>${newsDTO.news_writendate }</div>
      			<div id='${status.index }-2' class='main'>${newsDTO.news_title }</div>
      			<div id='${status.index }-3' class='cont'>
      			<c:choose>
      			 <c:when test="${fn:length(newsDTO.summ_content) gt 150 }">
      			 	${fn:substring(newsDTO.summ_content,0,150) }...
      			 </c:when>
     			  <c:otherwise>
     			  	${newsDTO.summ_content}
 					  </c:otherwise>
      			</c:choose>
      			<br/>
      			</div>
      		</a>
      		
     			<div class='bookmark-cover'>
     				<i class='fa-solid fa-bookmark' scrapNum='${status.index }' news='${newsDTO.news_key}'></i>
     			</div>
      	</div>
      </c:forEach> 
				
			</div> <!-- tab-1-keyword -->
		





			<div class="paging">
			<c:if test="${pageMaker.prev }">
				<a onclick="submitForm(${pageMaker.startPage-1 })">이전</a>&nbsp;
			</c:if>
			
			<c:forEach begin="${pageMaker.startPage }" end="${pageMaker.endPage }" var="i">
				<a class="movePage" onclick="submitForm(${i})">${i }</a>&nbsp;
			</c:forEach>
      <c:if test="${pageMaker.next && pageMaker.endPage > 0}">
        <a onclick="submitForm(${pageMaker.endPage+1 })">다음</a>
      </c:if>       
			
			</div>
<jsp:include page="/WEB-INF/views/search/scrapInSearchPage.jsp" />	






		</div>
	</div>
</div>

<script>
let noneKeyword = '${noneKeyword }';	

if(!noneKeyword) {
	
} else {
	console.log(noneKeyword);
	$("#noResult").html(
	"<ul style='list-style-type: circle; margin-top: 80px;'>"
	+"<li	stlye='margin: 5px 0;'>단어의 철자가 정확한지 확인해 보세요.</li>"
	+"<li	stlye='margin: 5px 0;'>한글을 영어로 혹은 영어를 한글로 입력했는지 확인해 보세요.</li>"
	+"<li	stlye='margin: 5px 0;'>검색어의 단어 수를 줄이거나, 보다 일반적인 검색어로 다시 검색해 보세요.</li>"
	+"<li	stlye='margin: 5px 0;'>두 단어 이상의 검색어인 경우, 띄어쓰기를 확인해 보세요.</li>"
	+"</ul>"
	)//html
}

</script>