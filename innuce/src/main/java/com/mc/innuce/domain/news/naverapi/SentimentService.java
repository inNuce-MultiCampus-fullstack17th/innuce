package com.mc.innuce.domain.news.naverapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * 네이버에서 감정분석 호출하는 서비스
 * @author JIN
 *
 */
@Service
public class SentimentService {


	private String clientId;
	private String clientSecret;

	// 싱글톤으로 관리되는 객체가 생성될 때 파일을 읽어서 기본 세팅값 설정
	public SentimentService() {
		super();
//
//		try {
//			String os = System.getProperty("os.name").toLowerCase();
//			String url = "";
//
//			if (os.contains("win")) {
//				url = "c:/fullstack/naverinform.properties";
//			} else {
//				url = "/home/ec2-user/properties/naverinform.properties";
//			}
//
//			InputStream is = new FileInputStream(new File(url));
//			Properties props = new Properties();
//			
//			props.load(is);
//			this.clientId = props.getProperty("naverClientID");
//			this.clientSecret = props.getProperty("naverClientSecret");
//		} catch (FileNotFoundException e) {
//			System.out.println("naverinform file이 없습니다.");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.out.println("SentimentService ERROR!");
//			e.printStackTrace();
//		}

	}

	public HashMap<String, String> sentiment(String sentence) {
		// 결과값은 맵으로 관리
		HashMap<String, String> result = new HashMap<>();

		// 970자 초과일 경우 970자 이하까지 문장단위로 자름.
		while (sentence.length() > 930)
			sentence = sentence.substring(0, sentence.lastIndexOf("."));
		
		try {
			String apiURL = "https://naveropenapi.apigw.ntruss.com/sentiment-analysis/v1/analyze";
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			con.setRequestProperty("Content-Type", "application/json");

			JSONObject json = new JSONObject();
			json.put("content", "\"" + sentence + "\"");

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(json.toString().getBytes("utf-8"));
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			BufferedReader br;
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 오류 발생
				System.out.println("error!!! responseCode = " + responseCode);
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();

			JSONObject document = new JSONObject(response.toString()).getJSONObject("document");

			String sentiment = document.getString("sentiment");
			double percent = document.getJSONObject("confidence").getBigDecimal(sentiment).doubleValue();

			result.put("sentiment", sentiment);
			result.put("percent", String.valueOf(percent));
		} catch (Exception e) {
			System.out.println(e);
		}

		return result;
	}

}