package com.mc.innuce.domain.news.selenium.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.NoSuchDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mc.innuce.domain.news.dto.NewsDTO;
import com.mc.innuce.domain.news.dto.NewsTemVO;
import com.mc.innuce.domain.news.naverapi.SentimentService;
import com.mc.innuce.domain.news.naverapi.SummaryService;
import com.mc.innuce.domain.news.selenium.webdriver.WebDriverPool;
import com.mc.innuce.domain.news.service.NewsService;
import com.mc.innuce.global.scheduler.NewsScheduler;
import com.mc.innuce.global.util.hreftonewsdto.WebConverter;
import com.mc.innuce.global.util.sqltojava.SqlConverter;

/**
 * 크롤링 전반에 대한 서비스 뉴스 카테고리, 키워드 검색 크롤링 지원
 * 
 * @author JIN
 */
@Service
public class CrawlingNewsService {

	@Autowired
	NewsService newsService;
	@Autowired
	SummaryService summaryService;
	@Autowired
	SentimentService sentimentService;
	@Autowired
	WebDriverPool dp;
	@Autowired
	WebConverter conv;
	private Set<String> dbHash = new HashSet<>();
	
	final private String naverNewsMainURI = "https://news.naver.com/main/main.naver?mode=LSD&mid=shm&sid1=";
	final private String prePage = "#&date=%2000:00:00&page=";
	final private String naverCategoryURI = "https://news.naver.com/section/";

	// https://search.naver.com/search.naver?where=news&sort=1&pd=3&
	final private String naverSearchURI = "https://search.naver.com/search.naver?where=news&sort=1&pd=3&";
	// query=?&ds=2024.01.19&de=2024.01.19

	// 하루 단위로 최대로 긁을 기사, 관심도 순으로 긁을 뉴스 기사 개수
	final private int limitCrawlingSearchSortDay = 1000;
	final private int limitCrawlingSearchSortInterest = 20;

	private void initDBHash() {
		if (dbHash.isEmpty()) {
			dbHash = initHashSetFromNewsKey();
			System.out.println("init " + printColor("dbHash", "red"));
			System.out.println("dbHash size : " + printColor("" + dbHash.size(), "green"));
		}
	}

	// 뉴스 카테고리 페이지에서 헤드라인만 추출하는
	public void crawlerHeadlineNews(String category, String categoryNumString) {
		initDBHash();

		WebDriver driver = WebDriverPool.getWebDriver();
		List<NewsTemVO> resultVOList = new ArrayList<>();
		List<NewsDTO> resultDTOList = new ArrayList<>();
		String url = naverCategoryURI + categoryNumString;
		// _SECTION_HEADLINE
		while (true) {
			try {
				driver.get(url);
				sleep(1000);

				List<WebElement> newsElementList = driver.findElements(By.className("_SECTION_HEADLINE"));
				List<WebElement> newsFilterList = new ArrayList<>();

				System.out.println("headline list size " + newsElementList.size());
				for (WebElement e : newsElementList)
					if (e.findElements(By.className("sa_thumb_link")).size() == 1)
						newsFilterList.add(e);

				System.out.println("filter headline list size " + newsElementList.size());

				SqlConverter sc = new SqlConverter();
				for (WebElement e : newsFilterList) {
					String href = e.findElement(By.className("sa_thumb_link")).getAttribute("href");
					String thumburl = e.findElement(By.tagName("img")).getAttribute("src");
					NewsDTO dto = new NewsDTO();

					dto.setNews_pulldate(sc.localDateToTimestamp(LocalDate.now()));
					dto.setNews_category(category);
					dto.setNews_key(conv.getLongNewsKey(href));
					resultDTOList.add(dto);

					if (dbHash.add(conv.get13NewsKey(href)))
						resultVOList.add(new NewsTemVO(href, thumburl));
				}

				break;
			} catch (Exception e) {
				System.out.println(printColor("ERROR HEADLINE", "red"));
				e.printStackTrace();
				driver.quit();
				driver = WebDriverPool.createNewWebDriver();
				continue;
			}
		}
		releaseDriver(driver);

		insertNewsFromVo(resultVOList);
		newsService.insertNewsHeadline(resultDTOList);
	}

	// 미리 정해둔 수만큼 병렬로 뉴스 파싱 처리
	public void manageNewsParsingTask() {

	}

	/**
	 * CATEGORY LOGIC START 서버 실행 시 dbHash 초기화 카테고리마다 카테고리별 페이지로 이동 후 db에 없는
	 * NewsTemVO를 크롤링 크롤링한 newsVOList에 대해 각 news 페이지로 이동, NewsDTO로 파싱 후 db에 저장
	 */
	// 네이버 페이지 변경(02-02)으로 인한 새롭게 작성된 카테고리 크롤링 코드
	public void crawllingCategoryNews() {
		initDBHash();
		String[] categorys = { "100", "101", "102", "103", "104", "105" };

		for (String category : categorys) {
			List<NewsTemVO> newsVOList = getNewsTemVOListPerCategory(category);

			System.out.println(printColor(category, "green") + " searching start");
			int insertSize = insertNewsFromVo(newsVOList);
			System.out.println(printColor(category, "green") + " VO size is " + printColor("" + newsVOList.size(), "green"));
			System.out.println(printColor(category, "green") + " insert size is " + printColor("" + insertSize, "green"));
		}
	}

	// 카테고리 페이지에서 VOList를 파싱하는 메소드
	public List<NewsTemVO> getNewsTemVOListPerCategory(String category) {
		WebDriver driver = WebDriverPool.getWebDriver();

		String url = naverCategoryURI + category;
		List<NewsTemVO> resultList = new ArrayList<>();

		int[] countMoreLimits = {100, 10, 5};
		int moreCategoryNewsClickCountLimit = 
				NewsScheduler.categoryCrawlerCallCount < 3 ? countMoreLimits[NewsScheduler.categoryCrawlerCallCount] : 2;
		// 작업이 끝까지 진행될 때까지 반복
		// 페이지 리로딩 없이 정상 실행 되었다면 반복 없이 break 문을 만나 종료
		while (true) {
			driver.get(url);
			sleep(1000);

			int clickCount = 0;
			try {
				// 기사 더보기 버튼 display option none이 될때까지 반복해서 누름
				while (driver.findElement(By.className("_CONTENT_LIST_LOAD_MORE_BUTTON")).isDisplayed()
						&& clickCount++ < moreCategoryNewsClickCountLimit) {
					driver.findElement(By.className("_CONTENT_LIST_LOAD_MORE_BUTTON")).click();
					
					sleep(500);
				}
			} catch (Exception e) {
				System.out.println("기사 더 보기 버튼 에러");
				e.printStackTrace();
				driver.quit();
				driver = WebDriverPool.createNewWebDriver();
				continue;
			}

			// 중간에 페이지 갱신으로 인해 초기화 된 경우 롤백하기 위한 try-catch
			// WebElement 요소들 추출하다가 에러 뜰 경우 페이지가 초기화 된 경우임으로 마찬가지로 반복
			try {
				System.out.println(printColor("start finding newsElementList", "gray"));

				// img 태그가 있긴 있지만 없어서 에러난 경우를 대비해 비어있는 거를 거르고 모음
				List<WebElement> originElementList = driver.findElements(By.className("sa_thumb_link"));

				List<WebElement> newsElementList = new ArrayList<>();
				for (WebElement e : originElementList)
					if (!e.findElements(By.tagName("img")).isEmpty())
						newsElementList.add(e);

				System.out.println(printColor("success finding newsElementList", "gray"));
				System.out.println("size is " + printColor("" + newsElementList.size(), "red"));

				// dbHash에 중복된 개수를 카운팅
				int falseCount = 0;
				int[] countFalseLimits = {200, 100, 50};
				int falseCountLimit = 
						NewsScheduler.categoryCrawlerCallCount < 3 ? countFalseLimits[NewsScheduler.categoryCrawlerCallCount] : 20;
				
				// 각 요소마다 필요한 값 추출 및 dbHash에 임시 저장
				// 저장에 성공했다면 VO객체로 파싱 후 resultList에 담음
				// 도중에 실패하더라도 dbHash에 저장된 값이 있으므로 다시 파싱 안하고 넘어감
				for (WebElement e : newsElementList) {
					String href = e.getAttribute("href");
					String thumburl = e.findElement(By.tagName("img")).getAttribute("src");

					if (dbHash.add(conv.get13NewsKey(href))) {
						resultList.add(new NewsTemVO(href, thumburl));
						if (falseCount > 0)
							falseCount--;
					} else {
						falseCount++;
					}

					// dbHash에 일정 이상의 중복된 값이 들어가려고 시도되면 추가 탐색 종료
					if (falseCount >= falseCountLimit)
						break;

				}
				break; // 정상 진행 되었다면 while 탈출
			} catch (Exception e) {
				System.out.println(printColor("This error may have occurred due to a forced page move.", "red"));
				System.out.println(printColor(driver.getCurrentUrl(), "red"));
				e.printStackTrace();
				driver.quit();
				driver = WebDriverPool.createNewWebDriver();
				continue; // 에러 잡혔다는 건 페이지 초기화 되었다는 뜻이므로 while 반복
			}
		}

		releaseDriver(driver);
		return resultList;
	}
	// CATEGORY LOGIC END

	// search logic start
	public List<NewsDTO> getSearchNews(String keyword) {
		LocalDate de = LocalDate.now(); // 검색 끝 날짜 de
		// 기본 날짜 검색으로는 7일의 텀까지 db에 저장
		LocalDate ds = de.minusDays(3); // 검색 시작 날짜 ds

		return getSearchNews(keyword, ds, de);
	}

	public List<NewsDTO> getSearchNewsDefault(String url) {
		WebDriver driver = WebDriverPool.getWebDriver();
		driver.get(url);

		List<NewsDTO> resultList = new ArrayList<>();
		List<NewsTemVO> newsVOList = new ArrayList<>();
		Set<String> dbHash = initHashSetFromNewsKey();

		int returnDefaultSize = 5;
		int retryCount = 0;
		int maxRetry = 5;
		while (true) {
			retryCount++;
			try {
				WebElement footer = driver.findElement(By.id("footer"));
				sleep(1000);
				scrollToElement(driver, footer);

				List<WebElement> bxList = driver.findElements(By.className("bx"));
				System.out.println("bxlist size : " + bxList.size());
				List<WebElement> filterList = new ArrayList<>();

				filterList.addAll(filterSuitableNewsElements(bxList));

				System.out.println("filterlist size : " + filterList.size());

				if (retryCount < maxRetry && filterList.size() < returnDefaultSize) {
					System.out.println("check");
					continue;
				}

				for (WebElement e : filterList) {
					String href = e.findElement(By.className("info_group")).findElements(By.tagName("a")).get(1)
							.getAttribute("href");

					if (dbHash.add(conv.get13NewsKey(href))) {
						String thumburl = e.findElement(By.className("dsc_thumb")).findElement(By.tagName("img"))
								.getAttribute("src");
						newsVOList.add(new NewsTemVO(href, thumburl));
					} else {
						resultList.add(newsService.selectOne(conv.get13NewsKey(href)));
					}

					if (newsVOList.size() + resultList.size() == returnDefaultSize)
						break;
				}
				System.out.println("newsvolistsize : " + newsVOList.size());
				if (!url.equals(URLDecoder.decode(driver.getCurrentUrl(), StandardCharsets.UTF_8.toString()))) {
					System.out.println(URLDecoder.decode(driver.getCurrentUrl(), StandardCharsets.UTF_8.toString()));
					System.out.println(url);
					driver.get(url);
					newsVOList = new ArrayList<>();
					continue;
				}

				if (newsVOList.size() + resultList.size() < returnDefaultSize && retryCount < maxRetry) {
					newsVOList = new ArrayList<>();
					continue;
				}

				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(driver.getCurrentUrl());
				sleep(1000);
			}
		}

		for (NewsTemVO vo : newsVOList)
			resultList.add(parseNewsToNewsDTO(driver, vo.getUrl(), vo.getThumburl()));

		newsService.insertNewsList(resultList);
		releaseDriver(driver);

		return resultList;
	}

	public List<NewsDTO> getSearchNews(String keyword, LocalDate ds, LocalDate de) {
		// 추후 에러 발생시키면서 날짜값 설정 잘못됐다고 알려줘야 함
		if (ds.isAfter(de)) {
			return null;
		}

		// db에 이미 검색된 적이 있는지 dto 객체를 가져옴
		WebDriver driver = WebDriverPool.getWebDriver();
		List<NewsDTO> resultList = new ArrayList<>();
		List<String> newsKeyList = new ArrayList<>();

		// KeywordDTO dto = crudService.oneKeyword(keyword);

		HashSet<String> dbHash = initHashSetFromNewsKey();
		// 검색된 적이 없다면(=db에서 검색된 객체가 없다면) dt에 검색 시작날짜와 de를 오늘날짜까지 지정
		// case 1 완전 최초 검색 - 기본 설정된 날짜로부터 오늘까지 싹다 긁어옴
		// case 2 url 파라미터 접근 - 접근을 시도한 url 파라미터에서 시작날짜로부터 끝날짜까지 전부 db에 insert.
//		if(dto == null) {
//			//crudService.insertKeyword(keyword, ds);
//			de = LocalDate.now();
//		} else {
//			// 검색된 적이 있다면 가장 오래전 검색된 날짜 전날로 de를 세팅 
//			de = dto.getKeyword_recent_time().toLocalDateTime().toLocalDate().minusDays(1);
//		}

		// 오늘자 검색
		newsKeyList = searchNews(driver, keyword, LocalDate.now());

		// ds를 기준으로 하루씩 검색
		while (!ds.isAfter(de)) {
			// resultList.addAll(searchNews(keyword, ds));
			ds = ds.plusDays(1);
		}

		WebDriverPool.releaseWebDriver(driver);

		return null;
	}

	// 현재 사용하지 않음
	private List<String> searchNews(WebDriver driver, String keyword, LocalDate date) {
		driver.get(naverSearchURI + parseQueryWithDate(keyword, date));

		long before = System.currentTimeMillis();
		// 기준점이 될 footer 선언
		WebElement footer = driver.findElement(By.id("footer"));
		List<WebElement> list = driver.findElements(By.className("bx"));
		// footer 까지 스크롤, 기존 리스트와 새로 얻은 리스트의 사이즈로 while문 탈출 여부 정함
		while (true) {
			scrollToElement(driver, footer);
			sleep(2000);
			List<WebElement> newList = driver.findElements(By.className("bx"));
			System.out.println("list size : " + list.size());
			if (list.size() == newList.size())
				break;
			list = newList;
		}
		Long after = System.currentTimeMillis();
		System.out.println("scroll time : " + (after - before));

		before = System.currentTimeMillis();
		List<WebElement> resultList = new ArrayList<>();
		resultList.addAll(filterSuitableNewsElements(list));
		after = System.currentTimeMillis();

		System.out.println("filter list size : " + resultList.size());
		System.out.println("stream time : " + (after - before));

		before = System.currentTimeMillis();
		resultList = new ArrayList<>();

		resultList.addAll(filterSuitableNewsElements(list));

		after = System.currentTimeMillis();
		System.out.println("filter list size : " + resultList.size());
		System.out.println("traditional for time : " + (after - before));
		return null;
	}

	// vo 객체를 dto로 파싱 후 db에 insert 하는 메소드
	public int insertNewsFromVo(List<NewsTemVO> voList) {
		WebDriver driver = WebDriverPool.getWebDriver();
		List<NewsDTO> resultList = new ArrayList<>();
		int resultSize = 0;

		for (int i = 0; i < voList.size(); i++) {
			
			while (true) {
				try {
					// 램 용량 최적화를 위한 10번 페이지 이동마다 드라이버 초기화
					if (i % 10 == 9) {
						driver = reload(driver);
					}
		
					resultList.add(parseNewsToNewsDTO(driver, voList.get(i).getUrl(), voList.get(i).getThumburl()));
					// 램 용량 최적화를 위한 10번의 객체 생성마다 resultList db에 insert 및 초기화
					if (resultList.size() > 9) {
						resultSize += newsService.insertNewsList(resultList);
						resultList = new ArrayList<>();
					}
					break ;
				} catch (Exception e) {
					
					System.out.println("insertNewsFromVO error" + voList.get(i).getUrl()); 
					System.out.println("insertNewsFromVO error");
				// need delete this!
          if (voList.get(i).getUrl().equals("https://n.news.naver.com/mnews/article/008/0005004100")) {
              System.out.println(printColor("CATCH ERROR PAGE", "red"));
              voList.remove(i--);
          }
					e.printStackTrace();
					driver.quit();
					driver = WebDriverPool.createNewWebDriver();
				}
			}
		}

		if (resultList.size() > 0)
			resultSize += newsService.insertNewsList(resultList);

		releaseDriver(driver);

		return resultSize;
	}

	public NewsDTO parseNewsToNewsDTO(WebDriver newsdriver, String href, String thumbnailuri) {
		NewsDTO news = new NewsDTO();
		newsdriver.get(href);
		
		news.setNews_key(conv.getLongNewsKey(href));
		news.setPress_key(conv.getIntPressKey(href));
		news.setNews_title(newsdriver.findElement(By.id("title_area")).getText());

		String content = newsdriver.findElement(By.id("dic_area")).getText();

		List<WebElement> tem = newsdriver.findElements(By.tagName("strong"));
		for (WebElement replaceText : tem)
			content = content.replace(replaceText.getText(), "");

		tem = newsdriver.findElements(By.className("img_desc"));
		for (WebElement replaceText : tem)
			content = content.replace(replaceText.getText(), "");

		news.setNews_content(content.strip());

		news.setNews_writendate(conv.getStringToTimestamp(
				newsdriver.findElement(By.className("_ARTICLE_DATE_TIME")).getAttribute("data-date-time")));
		List<WebElement> updatediv = newsdriver.findElements(By.className("_ARTICLE_MODIFY_DATE_TIME"));

		if (updatediv.size() == 0)
			news.setNews_updatedate(null);
		else
			news.setNews_updatedate(conv.getStringToTimestamp(updatediv.get(0).getAttribute("data-modify-date-time")));
		news.setNews_pulldate(new Timestamp(System.currentTimeMillis()));

		List<WebElement> writer = newsdriver.findElements(By.className("byline_s"));
		if (writer.size() == 0)
			news.setNews_writer("원문 참조");
		else
			news.setNews_writer(writer.get(0).getText());

		news.setNews_uri(href);
		news.setNews_originuri(newsdriver.findElement(By.className("media_end_head_origin_link")).getAttribute("href"));
		news.setNews_deleted(false);
		news.setNews_deleteddate(null);
		news.setNews_deletedcode(0);
		try {
			news.setNews_category(
					newsdriver.findElement(By.className("is_active")).findElement(By.tagName("span")).getText());
		} catch (Exception e) {
			news.setNews_category("미정");
		}
		news.setNews_thumbnailuri(thumbnailuri);
		List<WebElement> imgdiv = newsdriver.findElements(By.id("img1"));
		if (imgdiv.size() == 0)
			news.setNews_thumbnailuri2(null);
		else
			news.setNews_thumbnailuri2(newsdriver.findElement(By.id("img1")).getAttribute("src"));

//		news.setSumm_content(summaryService.summary(news.getNews_title(), news.getNews_content()));
		if (news.getSumm_content() == null)
			news.setSumm_content("원문참조");

		if (!news.getSumm_content().equals("원문참조")) {
			HashMap<String, String> sentimentMap = sentimentService.sentiment(news.getSumm_content());
			news.setSumm_sentiment(sentimentMap.get("sentiment"));
			if (news.getSumm_sentiment().equals("") || news.getSumm_sentiment() == null)
				news.setSumm_sentiment("원문참조");

			news.setSumm_sentimentpercent(sentimentMap.get("percent"));
			if (news.getSumm_sentimentpercent().equals("") || news.getSumm_sentimentpercent() == null)
				news.setSumm_sentimentpercent("원문참조");
		} else {
			news.setSumm_sentiment("");
			news.setSumm_sentimentpercent("");
		}
		return news;
	}

	// 네이버에서 키워드로 검색할때 조건에 맞는 기사를 필터링 하는 메소드
	private List<WebElement> filterSuitableNewsElements(List<WebElement> list) {
		// 네이버 뉴스가 있고 썸네일이 있는경우 필터
		return list.stream()
				.filter(e -> e.findElements(By.className("dsc_thumb")).size() == 1
						&& e.findElement(By.className("info_group")).findElements(By.tagName("a")).size() == 2) 
				.filter(e -> !e.findElement(By.className("info_group")).findElements(By.tagName("a")).get(1)
						.getAttribute("href").contains("sports") // 스포츠 뉴스 필터
						&& !e.findElement(By.className("info_group")).findElements(By.tagName("a")).get(1)
								.getAttribute("href").contains("sid=106")) // 연예란 필터
				.collect(Collectors.toList());
	}

	private HashSet<String> initHashSetFromNewsKey() {
		HashSet<String> dbHash = new HashSet<>();
		for (long i : newsService.getAllNewsListOnlyKey())
			dbHash.add(String.format("%013d", i));
		return dbHash;
	}

	// WebDriverPool에 드라이버 반환
	private void releaseDriver(WebDriver driver) {
		driver = reload(driver);
		WebDriverPool.releaseWebDriver(driver);
	}

	// driver에서 quit()을 호출함으로써 메모리 확보 후 새로 생성한 객체를 반환해줌.
	private WebDriver reload(WebDriver driver) {
		int count = 1;

		while (true) {
			try {
				try {
					driver.quit();
				} catch (Exception e) {
					System.out.println("driver quit failed");
				}
				sleep(500);

				driver = WebDriverPool.createNewWebDriver();
				break;
			} catch (NoSuchDriverException e) {
				System.out.println(printColor("ERROR", "red") + " : CrawlingNewsService.reload - retry count : "
						+ printColor(String.format("%03d", count++), "red"));
			} catch (Exception e) {
				System.out.println(printColor("ERROR unknown", "red") + " : CrawlingNewsService.reload - retry count : "
						+ printColor(String.format("%03d", count++), "red"));
			}
		}
		return driver;
	}

	private Map<String, String> colorMap = Map.of("reset", "\u001B[0m", "red", "\u001B[31m", "green", "\u001B[32m",
			"yellow", "\u001B[33m", "blue", "\u001B[34m", "gray", "\u001B[90m", "darkgray", "\u001B[2m");

	private String printColor(String str, String color) {
		return colorMap.get(color) + str + colorMap.get("reset");
	}

	// 특정 페이지에서 특정 요소까지 스크롤을 내림
	private void scrollToElement(WebDriver driver, WebElement element) {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
	}

	private String parseQueryWithDate(String keyword, LocalDate now) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

		return "query=" + keyword + "&ds=" + now.format(formatter) + "&de=" + now.format(formatter);
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}

	// 2024-02-02
	// 네이버 페이지 구성 변경으로 인해 죽은 코드들 고이 잠들다.
	public void getCategoryNews(String[] categorys) {
		// String[] categorys = {"100", "101", "102", "103", "104", "105"};
		Long before = System.currentTimeMillis();
		// category 별로 최대 페이지를 저장할 map
		Map<String, Integer> categoryPageLimits = new HashMap<>();

		// 카테고리 별로 최대 페이지를 받아와서 저장 및 최대 페이지 갱신
		int pageMax = 0;

		for (String category : categorys) {
			int pageLimit = getLimitPagePerCategory(category);

			categoryPageLimits.put(category, pageLimit);

			if (pageMax < pageLimit)
				pageMax = pageLimit;
		}

		// pageMax = 10; // test code delete this!
		// 100 카테고리 1페이지 101 카테고리 1페이지 ... 순으로 pageurl 리스트 생성
		List<String> pageUrlList = new ArrayList<>();

		for (int i = 1; i <= pageMax; i++) {

			for (String category : categorys) {
				int pageLimit = categoryPageLimits.get(category);

				if (i <= pageLimit)
					pageUrlList.add(naverNewsMainURI + category + prePage + String.format("%03d", i));
			}
		}

		// pageurl에서 db에 들어갈 수 있는 뉴스들에 해당하는 vo 리스트 생성
		// vo에는 dto 생성에 필요한 최소한의 정보인 네이버뉴스 직링크와 thmubnailurl 정보가 들어가있다.
		List<NewsTemVO> voList = getCategoryNews(pageUrlList);
		Long after = System.currentTimeMillis();
		System.out.println("size : " + voList.size() + ", paging time : " + (after - before));

		before = System.currentTimeMillis();
		int insertSize = insertNewsFromVo(voList);
		after = System.currentTimeMillis();
		System.out.println("size : " + insertSize + ", inserting time : " + (after - before));
	}

	public int getLimitPagePerCategory(String category) {
		WebDriver driver = WebDriverPool.getWebDriver();
		int limitPage;
		String url = naverNewsMainURI + category + prePage + "999";

		while (true) {
			try {
				driver.get(url);
				System.out.println("getLimitPagePerCategory\n" + url);
				sleep(1000);

				limitPage = Integer
						.parseInt(driver.findElement(By.id("paging")).findElement(By.tagName("strong")).getText());
				System.out.println(limitPage);
				break;
			} catch (Exception e) {
				e.printStackTrace();
				driver.get(url);
				sleep(1000);
			}
		}

		releaseDriver(driver);
		return limitPage;
	}

	public List<NewsTemVO> getCategoryNews(List<String> pageUrlList) {
		// searchall = 1, 중복페이지 만나면 스킵 option = 0 - 구현예정
		WebDriver driver = WebDriverPool.getWebDriver();
		int option = 1;

		HashSet<String> dbHash = initHashSetFromNewsKey();
		List<NewsTemVO> voList = new ArrayList<>();

		// categor에 해당하는 끝 페이지 가져옴
		for (int i = 0; i < pageUrlList.size(); i++) {
			if (i % 50 == 0) {
				driver = reload(driver);
			}
			// '' 안에 변수들 나머 지 양식 지켜야함
			// https://news.naver.com/main/main.naver?mode=LSD&sid1='category'#&date=%2000:00:00&page='pages'
			List<WebElement> dt = new ArrayList<>();
			String url = pageUrlList.get(i);
			int count; // db에 중복된 개수 !변수명 변경 필요!

			while (true) {
				int retryCount = 0;
				int maxRetryCount = 10;
				count = 0;

				// 웹드라이버에서 값 가져오기
				try {
					driver.get(url);
					int beforeSize;

					do {
						beforeSize = dt.size();
						sleep(2000);
						dt = driver.findElements(By.className("photo"));
					} while (dt.size() != 20 && beforeSize != dt.size());

					for (WebElement e : dt) {
						String href = e.findElement(By.tagName("a")).getAttribute("href");

						// 중복되지 않았다면 dbHash에 추가하고 dto 객체 파싱
						if (dbHash.add(conv.get13NewsKey(href))) {
							NewsTemVO vo = new NewsTemVO();
							vo.setUrl(href);
							vo.setThumburl(e.findElement(By.tagName("img")).getAttribute("src"));
							voList.add(vo);
//							NewsDTO dto = parseNewsToNewsDTO(driver, href, thumbnailuri);
//							resultList.add(dto);
						} else {
							count++;
						}

						// 만약 카테고리 페이지 내에서 강제이동이 한번이라도 일어났다면 다시 리트라이
						if (!driver.getCurrentUrl().equals(url))
							continue;
					}

					// 한 페이지마다 insert
//					if(resultList.size() != 0)
//						System.out.printf("insert %d complete %d\n", resultList.size(), newsService.insertNewsList(resultList));

					break; // 에러 없이 끝났다면 break로 탈출
				} catch (Exception e) {
					// 특정 페이지에서 일정 이상 반복되면 탈출
					retryCount++;
					if (retryCount > maxRetryCount) {
						printColor(url + "faild... pass", "red");
						break;
					}
				}
			}
			// option이 0일때 추가된 뉴스가 없다면 탐색 종료
			if (option == 0 && dt.size() == count) {
				System.out.printf("dt : %d | count : %d\n", dt.size(), count);
				break;
			}

		}
		releaseDriver(driver);
		return voList;
	}

}
