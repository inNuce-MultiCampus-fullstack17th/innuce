package com.mc.innuce.domain.news.selenium.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mc.innuce.domain.news.dto.NewsDTO;
import com.mc.innuce.domain.news.selenium.service.CrawlingNewsService;
import com.mc.innuce.domain.news.selenium.service.CrawlingPressService;
import com.mc.innuce.domain.news.selenium.service.SeleniumService;
import com.mc.innuce.domain.news.service.NewsService;
import com.mc.innuce.domain.news.service.PressService;
import com.mc.innuce.global.util.jsonparsefromdto.JSONParser;

@RestController
public class SeleniumController {

	@Autowired
	CrawlingNewsService cns;
	@Autowired
	CrawlingPressService cps;
	@Autowired
	NewsService ns;
	@Autowired
	PressService ps;
	@Autowired
	SeleniumService ss;

	JSONParser jp = new JSONParser();
	
	/**
	 * 크롤러 호출하는 mapping
	 * newsdto list를 파싱 혹은 db에서 가져와 json으로 반환.
	 * 설정한 기간만큼 db에 크롤링할 task들을 집어넣음
	 * @param keyword
	 */
	@GetMapping("searchcrawller") // post로 수정 필요?
	@ResponseBody
	public String search(
			@RequestParam String keyword, @RequestParam(defaultValue = "0") String sort, // 0 = 관련순, 1 = 최신순
			@RequestParam(defaultValue = "0") String pressopt, // 0 = 언론사 설정 x, 1 = 언론사 설정
			@RequestParam(defaultValue = "") String press, // 예를 들어 언론사 값 1이 sql에 있는 값이라면 01, 001 셋중 아무거나 보내줘도 됨
			// ds와 de는 localdate 양식 요구 yyyy-MM-dd. 안 보내준 값은 오늘로 초기화해서 설정 됨
			@RequestParam(defaultValue = "0") String periodopt, // 0 = 기간 설정 x, 3 = 기간 설정
			@RequestParam(defaultValue = "") String ds,
			@RequestParam(defaultValue = "") String de) {

		// String jsonResultString = ss.search(keyword, sort, pressopt, press, periodopt, ds, de);
		// ss.searchBack(keyword, ds, de);
		List<NewsDTO> list = cns.getSearchNewsDefault(ss.search(keyword, sort, pressopt, press, periodopt, ds, de));
		return new JSONParser().getJsonArrayNews(list).toString();
	}

}