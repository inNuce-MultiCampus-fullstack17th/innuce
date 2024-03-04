<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>



<div class="news-cover">
	<div class="news-content">
		<div class="cover">

			<div class="main-title">
				<p>${keyword }</p>
				<p>${noneKeyword }</p>
			</div>
			
			<p id='noResult'></p>
	
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
      			<br>
      			<%-- ${fn:length(newsDTO.summ_content) gt 150 ? substring(newsDTO.summ_content,0,150) +"..." : newsDTO.summ_content }<br/> --%>
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
					<a onclick="submitForm(${pageMaker.startPage-1 })">이전</a>&nbsp;&nbsp;&nbsp;
				</c:if>
				
				<c:forEach begin="${pageMaker.startPage }" end="${pageMaker.endPage }" var="i">
					<a class="movePage" onclick="submitForm(${i})">${i }</a>&nbsp;
				</c:forEach>
	      <c:if test="${pageMaker.next && pageMaker.endPage > 0}">
	        &nbsp;<a onclick="submitForm(${pageMaker.endPage+1 })">다음</a>
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
		+"<li	style='margin: 5px 0;'>단어의 철자가 정확한지 확인해 보세요.</li>"
		+"<li	style='margin: 5px 0;'>한글을 영어로 혹은 영어를 한글로 입력했는지 확인해 보세요.</li>"
		+"<li	style='margin: 5px 0;'>검색어의 단어 수를 줄이거나, 보다 일반적인 검색어로 다시 검색해 보세요.</li>"
		+"<li	style='margin: 5px 0;'>두 단어 이상의 검색어인 경우, 띄어쓰기를 확인해 보세요.</li>"
		+"</ul>"
		)//html
	}
	
</script>