package com.mc.innuce.domain.news.selenium.webdriver;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

/**
 * Chrome 브라우저로 Selenium WebDriver 인스턴스를 관리하기 위한 Pool
 * @author JIN
 */
@Component
public class WebDriverPool {

	private static final int MAX_POOL_SIZE = 2;
	private static final BlockingQueue<WebDriver> webDriverPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);

	// 서버가 실행되기 전 미리 생성
	static {
		for (int i = 0; i < MAX_POOL_SIZE; i++)
			webDriverPool.offer(createNewWebDriver());
	}

	// option을 준 웹드라이버 생성
	public static WebDriver createNewWebDriver() {
		ChromeOptions chromeOptions = new ChromeOptions();

		chromeOptions.addArguments("--lang=ko_KR.utf-8");
		chromeOptions.addArguments("--incognito"); // 시크릿모드로 열기
		chromeOptions.addArguments("--disable-extensions"); // 확장기능 비활성화
		chromeOptions.addArguments("--disable-dev-shm-usage"); // 공유 메모리 사용 비활성화
		chromeOptions.addArguments("--disable-gpu"); // gpu 사용 x
		chromeOptions.addArguments("--disable-popup-blocking"); // 팝업 무시
		chromeOptions.addArguments("--headless");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-application-cache");
		chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		
		WebDriver driver;
		int retryCount = 0;
		
		
		while (true) {
			try {
				driver = new ChromeDriver(chromeOptions);
				Thread.sleep(500);
				
				break;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("retryCount : " + ++retryCount);
			}
		}
		return driver;
	}
	
	public static WebDriver getWebDriver() {
		WebDriver driver = null;
		
		try {
			driver = webDriverPool.take();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
		
		return driver;
	}

	public static void releaseWebDriver(WebDriver webDriver) {
		if (webDriverPool.size() < MAX_POOL_SIZE)
			webDriverPool.offer(webDriver);
		else
			webDriver.quit();
	}

	public void showStatusWebDriverPool() {
		System.out.println("WebDriverPool have " + webDriverPool.size());
		
		ArrayList<WebDriver> list = new ArrayList<>();
		
		int size = webDriverPool.size();
		for (int i = 0; i <size; i++) {
			try {
				list.add(webDriverPool.take());
				System.out.println("take");
			} catch (Exception e) {
				System.out.println("releaseWebDriver ERROR");
				e.printStackTrace();
			}
		}
		
		System.out.println("WebDriverList have " + list.size());
		for (WebDriver driver : list) {
			System.out.println("currenturl" + driver.getCurrentUrl());
			releaseWebDriver(driver);
		}
	}
	
	public void initWebDriverPool() {
		System.out.println("init driverpool");
		System.out.println("size " + webDriverPool.size());
		while (webDriverPool.size() > 0) {
			try {
				webDriverPool.take().quit();
				System.out.println("size " + webDriverPool.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < MAX_POOL_SIZE; i++) {
			System.out.println("creating...");
			webDriverPool.offer(createNewWebDriver());
			System.out.println(webDriverPool.size());
		}
		
	}
}