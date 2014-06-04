package com.eyougo.rollcrawler.parser;

import java.util.List;

public interface UrlParser {
	/**
	 * 从一个url中解析出包含的网页url
	 * @param url
	 * @return
	 */
	List<String> parseUrls(String url);
	
	/**
	 * 判断一个url是否可访问
	 * @param url
	 * @return
	 */
	boolean isValidUrl(String url);


    String getUrlHost(String url);
}
