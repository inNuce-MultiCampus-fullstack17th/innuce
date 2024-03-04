package com.mc.innuce.domain.search.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mc.innuce.domain.news.dto.NewsDTO;
import com.mc.innuce.domain.news.service.NewsService;
import com.mc.innuce.domain.search.dto.KeysDTO;
import com.mc.innuce.domain.search.dto.KeywordDTO;
import com.mc.innuce.domain.search.dto.SearchDTO;
import com.mc.innuce.domain.search.paging.PageMaker;
import com.mc.innuce.domain.search.service.ComponentService;
import com.mc.innuce.domain.search.service.GeolocationService;
import com.mc.innuce.domain.user.dto.UserDTO;
import com.mc.innuce.global.util.komoran.KomoranModel;
import com.mc.innuce.global.util.sqltojava.SqlConverter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

@Controller
public class SearchController {


	@Autowired
	private GeolocationService geoService;
	@Autowired
	private ComponentService service;
	@Autowired
	NewsService newsService;

	private String ip = "";

	private String myLocation = "";
	private final int PAGECOUNT = 10;

	private List<String> placeList = new ArrayList<>();

	@RequestMapping(value={"/","/main"})
	public ModelAndView main() {
		List<String> keywordKey = newsService.getKeywordNews2();
		ModelAndView mv = new ModelAndView();

		mv.addObject("keywordKeys", keywordKey);
		mv.setViewName("main");

		return mv;
	}

	@GetMapping("/search")
	public ModelAndView mainSearch(String keyword, HttpServletRequest request, HttpSession session,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
			@RequestParam(value = "pressString", required = false, defaultValue = "") String pressString,
			@RequestParam(value = "ds", required = false, defaultValue = "") String ds,
			@RequestParam(value = "de", required = false, defaultValue = "") String de) throws Exception {
		String ip = "";
		if (ip.equals("") || ip.equals(" ") || ip == null) {
			ip = getIp(request);
		}

		// seo start
		// 언론사 옵션
		int pressOption = 0;
		List<Integer> pressKeyList = new ArrayList<>();

		if (!pressString.isEmpty()) {
			pressOption = 1;
			String[] strList = pressString.split(",");

			for (String str : strList)
				pressKeyList.add(Integer.parseInt(str));
		}

		// 기간 옵션
		int periodOption = 0;
		if (!ds.isEmpty() && !de.isEmpty())
			periodOption = 1;
		// seo end

		ModelAndView mv = new ModelAndView();

		int totalCount = 0;
		int keywordKey = 0;
		int[] limit = new int[2];
		List<NewsDTO> newsList = new ArrayList<>();
		List<Integer> keywordKeyList = new ArrayList<>();

		KeywordDTO kDTO = null;
		UserDTO uDTO = null;
		SearchDTO sDTO = null;

//	keyword에 " "이 있을 때만 코모란을 돌리자? x
//		String path = System.getProperty("user.dir");

//		komoran.setFWDic(path + "/src/main/resources/static/dictionary/fwd.user");

		KomoranResult komoranResult = KomoranModel.getInstance().getKomoran().analyze(keyword);

//		일반명사NNG
//		고유명사NNP
		List<String> analyzeList = komoranResult.getMorphesByTags("NNP", "NNG");

//		코모란이 인식x : 영단어 
		if (analyzeList.isEmpty()) {
			analyzeList.add(keyword);
		}

		for (String token : analyzeList) {
			List<Long> newsKeyList = service.getNewsKeys(token);
			System.out.println("newsKeyList : " + newsKeyList);
			if (newsKeyList.isEmpty() || newsKeyList == null) {
				System.out.println(newsKeyList + " : newsKeyList 가 비어있습니다.");
			} else {
				if (token.length() >= 2) {

					System.out.println("===" + token + "===");
					kDTO = service.oneKeyword(token);
					System.out.println(kDTO);
					// keyword | keyword_news 테이블
					if (kDTO != null) {
						// token이 있으면
						// Keyword 테이블
						System.out.println(token + " 존재");
						keywordKeyList.add(kDTO.getKeyword_key());
						keywordKey = kDTO.getKeyword_key();
						
						

					} else {
						// token이 없으면 - insert
						// Keyword 테이블
						System.out.println(token + " 존재x");
						service.insertKeyword(token);
						System.out.println("insertKeyword 완료");

						kDTO = service.oneKeyword(token);
						keywordKeyList.add(kDTO.getKeyword_key());
						keywordKey = kDTO.getKeyword_key();


					}
					KeysDTO keys = new KeysDTO(keywordKey, newsKeyList);
					
					int s = service.insertKeywordNews(keys);
					System.out.println(s + ": insertKeywordNews 완료");
					
					System.out.println("keyword_key : " + keywordKey);
					System.out.println("ip : " + ip);
					// Search 테이블 - 유저에 따라 insert | 

					if (session.getAttribute("login_user") != null) {
						System.out.println("user 존재");
						// userDTO가 존재
						uDTO = (UserDTO) session.getAttribute("login_user");
						// keyword_key / userKey / ip 를 search 에 저장.
						sDTO = new SearchDTO(keywordKey, uDTO.getUser_key(), ip);
						System.out.println("난 sdto" + sDTO);
						SearchDTO oneSearchDTO = service.oneSearch(sDTO);
						System.out.println("oneSearch 완료" + oneSearchDTO);

						if (oneSearchDTO != null) {
							int i = service.updateSearch2(sDTO);
							System.out.println(i + " userDTO가 존재o k-u-c 존재o updateSearch완료");
						} else {
							int i = service.insertSearch(sDTO);
							System.out.println(i + " userDTO가 존재o k-u-c 존재x insertSearch완료");
						}

					} else {
						// userDTO가 존재 x
						System.out.println("user 존재x");
						sDTO = new SearchDTO(keywordKey, ip);
						System.out.println("난 sdto" + sDTO);
						SearchDTO oneSearchDTO = service.oneSearch2(sDTO);
						System.out.println("oneSearch2 완료 " + oneSearchDTO);
						
						if (oneSearchDTO != null) {
							// ip == clinet_key 가 존재
							int i = service.updateSearch(sDTO);
							System.out.println(i + " userDTO가 존재x k-c 존재o updateSearch완료");
						} else {
							// client_key 존재 x
							int i = service.insertSearch2(sDTO);
							System.out.println(i + " userDTO가 존재x k-c 존재x insertSearch2완료");
						}
						
					}

					// seo start
					if (pressOption == 0 && periodOption == 0)
						totalCount += service.getTotalNews(keywordKey);
					else if (pressOption == 0 && periodOption == 1)
						totalCount += service.getTotalNewsOptionPeriod(keywordKey, ds, de);
					else if (pressOption == 1 && periodOption == 0)
						totalCount += service.getTotalNewsOptionPress(keywordKey, pressKeyList);
					else if (pressOption == 1 && periodOption == 1)
						totalCount += service.getTotalNewsOptionPeriodPress(keywordKey, ds, de, pressKeyList);
					// seo end

				} // token >= 2
			} // newsKeyList is null or isEmpty
		} // for (String token : analyzeList)
		System.out.println(totalCount + " : totalCount");
		PageMaker pageMaker = new PageMaker(pageNum, totalCount);

		if (keywordKeyList.isEmpty()) {
			System.out.println("keywordKeyList 가 비어있습니다.");
			mv.addObject("noneKeyword", "\"" + keyword + "\"" + "에 대한 검색결과가 없습니다.");
		} else {

//	paging
			Map<String, Object> map = new HashMap<>();

			limit[0] = (pageNum - 1) * pageMaker.getPAGECOUNT();
			limit[1] = pageMaker.getPAGECOUNT();

			map.put("keyword_key", keywordKeyList);

			map.put("num1", limit[0]);
			map.put("num2", limit[1]);
			System.out.println("totalCount : " + totalCount);

//	키워드에 해당하는 news 가져오기
			// seo start
			if (pressOption == 0 && periodOption == 0) {
				newsList = service.getNewsListLimit(map);
			} else if (pressOption == 0 && periodOption == 1) {
				map.put("ds", new SqlConverter().localDateTimeToTimestamp(LocalDate.parse(ds).atTime(0, 0, 0)));
				map.put("de", new SqlConverter().localDateTimeToTimestamp(LocalDate.parse(de).atTime(23, 59, 59)));

				mv.addObject("ds", ds);
				mv.addObject("de", de);
				newsList = service.getNewsListLimitOptionPeriod(map);
			} else if (pressOption == 1 && periodOption == 0) {
				map.put("pressKeyList", pressKeyList);

				mv.addObject("pressKeyList", pressKeyList);
				newsList = service.getNewsListLimitOptionPress(map);
			} else if (pressOption == 1 && periodOption == 1) {
				map.put("pressKeyList", pressKeyList);
				map.put("ds", new SqlConverter().localDateTimeToTimestamp(LocalDate.parse(ds).atTime(0, 0, 0)));
				map.put("de", new SqlConverter().localDateTimeToTimestamp(LocalDate.parse(de).atTime(23, 59, 59)));

				mv.addObject("ds", ds);
				mv.addObject("de", de);
				mv.addObject("pressKeyList", pressKeyList);
				newsList = service.getNewsListLimitOptionPressPeriod(map);
			}
			// seo end

			mv.addObject("newsList", newsList);
			mv.addObject("keyword", keyword);
		}

		mv.addObject("pageMaker", pageMaker);
		mv.setViewName("search/searchPage");

		return mv;

	}

	@RequestMapping("/myLocation")
	ModelAndView myLocation(@RequestParam(value = "location", required = false, defaultValue = "") String location,
			HttpServletRequest request, HttpSession session,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum) throws Exception {

		System.out.println("first location : " + location);
		String ip = getIp(request);
		ModelAndView mv = new ModelAndView();


		if (myLocation.equals("")) {
			location = geoService.test(ip);

			JSONParser parser = null;
			JSONObject result = null;
			JSONObject geoLocation = null;

			String r1 = "";
			String r2 = "";
			String r3 = "";

			try {

				parser = new JSONParser();
				result = (JSONObject) parser.parse(location);
				geoLocation = (JSONObject) result.get("geoLocation");

				r1 = (String) geoLocation.get("r1");
				r2 = (String) geoLocation.get("r2");
				r3 = (String) geoLocation.get("r3");
				placeList.add(r1);
				placeList.add(r2);
				setMyLocation(r1 + " " + r2 + " " + r3);

			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		System.out.println("placeList ; " + placeList);
		System.out.println("ip : " + ip);

		System.out.println("myLocation : " + myLocation);

		int keywordKey = 0;
		int totalCount = 0;
		List<Integer> keywordKeyList = new ArrayList<>();
		List<NewsDTO> newsList = new ArrayList<>();
		KeywordDTO dto = null;

		for (String place : placeList) {

//			List<Long> newsKeyList = service.getNewsKeys2(place);

			if (place.equals("") || place.equals(" ") || place == null) {
				mv.addObject("placeMassage", "위치정보가 확인이 안됩니다.");
				mv.setViewName("main");
				return mv;
			}

			System.out.println("===" + place + "===");
			dto = service.oneKeyword(place);
			System.out.println("keywordDTO :" + dto);

			if (dto != null) {
				// place이 있으면
				keywordKeyList.add(dto.getKeyword_key());
				keywordKey = dto.getKeyword_key();

			} else {
				// place이 없으면 - insert
				service.insertKeyword(place);

				dto = service.oneKeyword(place);
				keywordKeyList.add(dto.getKeyword_key());
				keywordKey = dto.getKeyword_key();

			}
			System.out.println("news_key : " + service.getNewsKeys2(place));
			
			KeysDTO keys = new KeysDTO(keywordKey, service.getNewsKeys2(place));
			
			service.insertKeywordNews(keys);

			
			totalCount += service.getTotalNews(keywordKey);

		} // for (String place : analyzeList)

//	paging

		PageMaker pageMaker = new PageMaker(pageNum, totalCount);

		Map<String, Object> map = new HashMap<>();

		int[] limit = new int[2];

		limit[0] = (pageNum - 1) * pageMaker.getPAGECOUNT();
		limit[1] = pageMaker.getPAGECOUNT();

		map.put("keyword_key", keywordKeyList);

		map.put("num1", limit[0]);
		map.put("num2", limit[1]);

		System.out.println("totalCount : " + totalCount);

//	키워드에 해당하는 news 가져오기

		if (newsList.isEmpty()) {
			newsList = service.getNewsListLimit(map);
		}

		if (newsList.isEmpty()) {
			mv.addObject("noneKeyword", "\"" + myLocation + "\"" + "에 대한 검색결과가 없습니다.");
		} else {
			mv.addObject("keyword", myLocation);
		}

		mv.addObject("newsList", newsList);
		mv.addObject("pageMaker", pageMaker);
		mv.setViewName("search/myLocation");
		return mv;

	}

	private String getIp(HttpServletRequest request) throws UnknownHostException {

		ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-RealIP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("REMOTE_ADDR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
			InetAddress address = InetAddress.getLocalHost();
			ip = address.getHostAddress();

		}
		return ip;
	}

	@RequestMapping("/news")
	public ModelAndView showNews(String newsKey) {
		ModelAndView mv = new ModelAndView();
//		해당 newsDTO 가져오기
		NewsDTO dto = service.oneNews(newsKey);

		mv.addObject("News", dto);
		System.out.println(dto);
		mv.setViewName("search/news");

		return mv;

	}

	public String getMyLocation() {
		return myLocation;
	}

	public void setMyLocation(String myLocation) {
		this.myLocation = myLocation;
	}

}
