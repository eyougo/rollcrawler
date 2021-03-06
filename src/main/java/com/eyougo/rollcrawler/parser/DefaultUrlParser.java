package com.eyougo.rollcrawler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eyougo.rollcrawler.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DefaultUrlParser implements UrlParser{
	private static HttpClient httpClient = HttpClientUtil.getHttpClient();
	private static final Log LOG = LogFactory.getLog(DefaultUrlParser.class);
	@Override
	public List<String> parseUrls(String url) {
		List<String> urls = new ArrayList<String>();
		List<String> allUrls = new ArrayList<String>();
		try {
			Document doc = Jsoup.connect(url).get();
			Elements links = doc.select("a[href]");
			for (Element link : links) {
                String childUrl = link.attr("abs:href");
                if (StringUtils.isNotEmpty(childUrl)
                        && !StringUtils.equalsIgnoreCase(url, childUrl)) {
                    allUrls.add(childUrl);
                }
			}
		} catch (IOException e) {
			
		}
		//去掉重复的
		for (String childUrl : allUrls) {
			if(!urls.contains(childUrl)){
				urls.add(childUrl);
			}
		}
		return urls;
	}

	@Override
	public boolean isValidUrl(String url) {
		HttpEntity entity = null;
        HttpGet httpGet= null;
        try {
            httpGet= new HttpGet(url);
			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String contentType = response.getLastHeader(HttpHeaders.CONTENT_TYPE).getValue();
				entity = response.getEntity();
				if (StringUtils.startsWithIgnoreCase(contentType, "text/")) {
					return true;
				}else {
					return false;
				}
			}
		} catch (Exception e) {
			LOG.error(url + e.getMessage(), e);
			return false;
		} finally {
			try {
				EntityUtils.consume(entity);
			} catch (IOException e) {
			}
            if (httpGet != null) {
                httpGet.abort();
            }
		}
		return false;
	}

    public String getUrlHost(String url) {
        return getHost(url);
    }

    public static String getTopDomain(String url){
        String domainSuffix = "(\\.com\\.cn|\\.net\\.cn|\\.org\\.cn|\\.gov\\.cn|\\.com|\\.net|\\.cn|\\.org|\\.cc|\\.me|\\.tel|\\.mobi|\\.asia|\\.biz|\\.info|\\.name|\\.tv|\\.hk|\\.公司|\\.中国|\\.网络)";
        try {
            HttpGet httpGet = new HttpGet(url);
            String host = httpGet.getURI().getHost().toLowerCase();
            Pattern pattern = Pattern.compile("[^\\.]+" + domainSuffix +
                    "(?![^\\.]+" + domainSuffix +")");

            Matcher matcher = pattern.matcher(host);

            List<String> findList = new ArrayList<String>();

            while (matcher.find()) {
                findList.add(matcher.group());
            }
            if (findList.size() > 0){
                return findList.get(findList.size() -1);
            }
            return "";
        }catch (Exception e){
            return "";
        }
    }

    public static String getHost(String url) {
        try {

            HttpGet httpGet= new HttpGet(url);
            return httpGet.getURI().getScheme()+ "://" + httpGet.getURI().getHost();
        }catch (Exception e){
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println(getTopDomain("http://haidian.cn2che.com/sellcarinfo_1820501.html"));
    }
}
