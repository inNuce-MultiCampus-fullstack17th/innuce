

/*keyword*/
$.ajax({
	url: "/keyword",
	type: "get",
	dataType: "json",
	success: function(response) {
		for (let i = 0; i < 3; i++) { // categoryIdx
			for (let j = 0; j < 5; j++) { // listIdx
				// categoryidx-listidx-(a|img|date|main|cont)
				let n = response[i][j]; // news
				// 인덱스 보정
				let categoryIdx = i + 1;
				let listIdx = j + 1;
				let idx = categoryIdx + '-' + listIdx;
				
				/*$('#' + idx + '-a').attr('href', 'news/' + n.news_key);*/
				$('.' + idx + '-a-keyword').attr('href', 'news?newsKey=' + n.news_key);
				$('#' + idx + '-img-keyword').attr('src',
					n.news_thumbnailuri2 == null ? n.news_thumbnailuri : n.news_thumbnailuri2);

				// Posted July 11, 2017
				let date = new Date(n.news_writendate);
				// 'ko-KR'로 해도 되는데 원본이 en-US로 되어있어서 en-US로 작성
				let formattedDate = date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
				console.log(formattedDate);
				$('#' + idx + '-date-keyword').text("Posted " + formattedDate);

				/*$('#' + idx + '-main-keyword').attr('href', 'news?newsKey=' + n.news_key);*/
				$('#' + idx + '-main-keyword').text(n.news_title);
				$('#' + idx + '-main-keyword').css({
					'color': '#333',
					'text-decoration': 'none'
				});

				let summ = n.summ_content;
				summ = summ.length > 150 ? summ.slice(0, 150) + '...' : summ;
			/*	$('#' + idx + '-cont').attr('href', 'news/' + n.news_key);*/
				$('#' + idx + '-main-keyword').attr('href', 'news?newsKey=' + n.news_key);
				$('#' + idx + '-cont-keyword').text(summ);
				$('#' + idx + '-cont-keyword').css({
					'color': '#333',
					'text-decoration': 'none'
				});
				$('#' + idx + '-scrap-keyword').attr('news',n.news_key)
				$
			}
		}
	},
	error: function() {
		alert("정상적이지 않은 요청입니다. 다시 시도해주세요");
	}, complete: function(){
		// 본인이 스크랩한 기사들은 표시해서 보여주기
		if(user_id != null){
		
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
				// 모든 북마크들을 찾아서 하나씩 돌아다니면서 북마크 체크표시 해주기
				let keyword_count = 3;
				let news_per_keyword_count =5;
				for(var i = 1; i < (keyword_count)*(news_per_keyword_count)+ 1; i++){
				
				// 해당 뉴스기사의 news attribute (news_key)가 scrapList안에 있으면 북마크를 체크표시로 바꾸기
				if(scrap_list.includes(Number($("[scrapNum="+i+"]").attr('news')))){
					$("[scrapNum="+i+"]").attr("class",'fa-solid fa-bookmark checked')
					
				}
				else{
					$("[scrapNum="+i+"]").attr("class",'fa-solid fa-bookmark')
					
				}//else
				}//for
					
		},//success
		error:{},
	})//ajax
	}//if
	}// complete
	
});

/* seo start */
// category
$.ajax({
	url: "/headline",
	type: "get",
	dataType: "json",
	success: function(response) {
		for (let i = 0; i < 6; i++) { // i = categoryIdx
			for (let j = 0; j < 5; j++) { // j = listIdx
				// categoryidx-listidx-(a|img|date|main|cont)
				let n = response[i][j]; // news
				// 인덱스 보정
				let categoryIdx = i + 1;
				let listIdx = j + 1;
				let idx = categoryIdx + '-' + listIdx;
				
				$('.' + idx + '-a-category').attr('href', 'news?newsKey=' + n.news_key);
				$('#' + idx + '-img-category').attr('src',
					n.news_thumbnailuri2 == null ? n.news_thumbnailuri : n.news_thumbnailuri2);

				// Posted July 11, 2017
				let date = new Date(n.news_writendate);
				// 'ko-KR'로 해도 되는데 원본이 en-US로 되어있어서 en-US로 작성
				let formattedDate = date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
				
				$('#' + idx + '-date-category').text("Posted " + formattedDate);
				$('#' + idx + '-main-category').text(n.news_title);
				$('#' + idx + '-main-category').css({
					'color': '#333',
					'text-decoration': 'none'
				});

				let summ = n.summ_content;
				summ = summ.length > 150 ? summ.slice(0, 150) + '...' : summ;
				/*$('#' + idx + '-cont-category').attr('href', 'news?newsKey=' + n.news_key);*/
				$('#' + idx + '-cont-category').text(summ);
				$('#' + idx + '-cont-category').css({
					'color': '#333',
					'text-decoration': 'none'
				});
				$('#' + idx + '-scrap-category').attr('news',n.news_key)
			}
		}
	},
	error: function() {
		alert("정상적이지 않은 요청입니다. 다시 시도해주세요");
	}, complete: function(){
		// 본인이 스크랩한 기사들은 표시해서 보여주기
		if(user_id != null){
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
				// 모든 북마크들을 찾아서 하나씩 돌아다니면서 북마크 체크표시 해주기
				let category_count = 6;
				let news_per_category_count =5;
				
				for(var i = 1; i < (category_count)*(news_per_category_count)+ 1; i++){
				
				// 해당 뉴스기사의 news attribute (news_key)가 scrapList안에 있으면 북마크를 체크표시로 바꾸기
				if(scrap_list.includes(Number($("[scrapNum="+(i+15)+"]").attr('news')))){
					$("[scrapNum="+(i+15)+"]").attr("class",'fa-solid fa-bookmark checked')
					
				}
				else{
					$("[scrapNum="+(i+15)+"]").attr("class",'fa-solid fa-bookmark')
					
				}//else
				}//for
			
		},//success
			error:{}
	})//ajax
	}//if
	}// complete
	
});

/*$('.tab-link').click(function() {
	$('.tab-link').css({
		'font-size': '20px',
		'color': '#5A6372'
	});
	$('.tab-link.current').css({
		'color': '#333',
		'font-size': '22px'
	});
});*/
/* seo end */

$('main i').on('click',function(){
		
		// 세션에 로그인된 유저가 없으면 스크랩 기능을 막기 위한 코드
		if(user_id == null ){
			var user_will_login = window.confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")
			// 로그인 하겠다고 하는 경우
			if(user_will_login == true){
				location.href ='/login'
			}
			// 로그인 안한다고 하는경우 (그냥 confirm창만 닫아주면 됨.)
			else{}
		}//if empty
		
		// 스크랩 기능 및 취소 기능
		else{
			
			
			// 이미 스크랩 된 기사인경우
			if($(this).attr("class")=='fa-solid fa-bookmark checked'){
				
			// 북바크 색상 변경
			$(this).attr("class",'fa-solid fa-bookmark')
			// 북마크 취소 했다는 사실 서버/db에 알리기
			$.ajax({
				type : 'post',
				url : 'scrapnewscancel',
				data : {'user_id' : user_id, 'news_key' : $(this).attr('news')},
				success : function(response){
					alert("스크랩이 취소 됐습니다")
					
				}//success
			})//ajax
		} // if class==checked
			// 이미 스크랩된 기사가 아닌경우
			else{
				
				// 북바크 색상 변경
				$(this).attr("class",'fa-solid fa-bookmark checked')
			
				// 북마크 했다는 사실 서버/db에 알리기
				$.ajax({
					type : 'post',
					url : 'scrapnews',
					data : {'user_id' : user_id, 'news_key' : $(this).attr('news')},
					success : function(response){
						alert("스크랩이 완료 됐습니다")
						
					}//success
				})//ajax
			}//else class==checked
		}//else empty
	})// main i click









