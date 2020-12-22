package com.deep007.mitmproxyjava.filter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.deep007.mitmproxyjava.modle.FlowRequest;
import com.deep007.mitmproxyjava.modle.FlowResponse;
import com.google.common.collect.Sets;

public class CookieCollectFilter implements FlowFilter {
	
	private Pattern SETCOOKIE_NAME_VALUE_MATCHER = Pattern.compile("([^\\s]+)=([^\\s,;]+)");
	
	private Pattern URL_DOMAIN_MATCHER = Pattern.compile("http[s]?://([^\\.]+[^/]+)");
	
	public Map<String,Cookie> catchCookies = new ConcurrentHashMap<>();
	
	public String domainFromUrl(String url) {
		Matcher sendcookie_domain_matcher = URL_DOMAIN_MATCHER.matcher(url);
		if (!sendcookie_domain_matcher.find()) {
			System.err.println("从url解析domain失败:"+url);
			return "baidu.com";
		}
		return sendcookie_domain_matcher.group(1);
	}

	@Override
	public void filterRequest(FlowRequest flowRequest) {
		String sendCookie = flowRequest.getHeaders().get("Cookie");
		if (sendCookie == null || sendCookie.isEmpty()) {
			//System.out.println("filterRequest cookie isEmpty");
			//System.out.println(flowRequest.getHeaders().keySet());
			return;
		}
		String url = flowRequest.getUrl();
		String domain = domainFromUrl(url);
		String[] items = sendCookie.split(";");
		for (int i = 0; items != null && i < items.length; i++) {
			String item = items[i].trim();
			if (item.contains("=")) {
				String[] nameValue = item.split("=", 2);
				Cookie cookie = new Cookie(nameValue[0], nameValue[1]);
				cookie.setDomain(domain);
				catchCookies.put(nameValue[0], cookie);
			}else if (!item.isEmpty()) {
				//log.warn("sendCookie解析错误:" + item);
			}
		}
	}

	@Override
	public void filterResponse(FlowResponse flowResponse) {
		String setCookie = flowResponse.getHeader("Set-Cookie");
		if (setCookie == null || setCookie.isEmpty()) {
			//System.out.println("filterResponse cookie isEmpty");
			//System.out.println(flowResponse.getHeaders().keySet());
			return;
		}
		String url = flowResponse.getRequest().getUrl();
		//clueSourceCode=%2A%2300; path=/; domain=.guazi.com, cainfo=%7B%22ca_s%22%3A%22self%22%2C%22ca_n%22%3A%22self%22%2C%22ca_medium%22%3A%22-%22%2C%22ca_term%22%3A%22-%22%2C%22ca_content%22%3A%22-%22%2C%22ca_campaign%22%3A%22-%22%2C%22ca_kw%22%3A%22-%22%2C%22ca_i%22%3A%22-%22%2C%22scode%22%3A%22-%22%2C%22ca_city%22%3A%22bj%22%2C%22guid%22%3A%220a87e81e-abdf-4a7e-c62b-f28209884750%22%2C%22keyword%22%3A%22-%22%2C%22ca_keywordid%22%3A%22-%22%2C%22ca_b%22%3A%22-%22%2C%22ca_a%22%3A%22-%22%2C%22display_finance_flag%22%3A%22-%22%2C%22platform%22%3A%221%22%2C%22version%22%3A1%2C%22client_ab%22%3A%22-%22%2C%22sessionid%22%3A%22ab0ddad4-451e-42a1-f56f-71067cb9e087%22%7D; expires=Wed, 22-Dec-2021 12:01:40 GMT; Max-Age=31536000; path=/; domain=.guazi.com, user_city_id=12; expires=Wed, 23-Dec-2020 12:01:40 GMT; Max-Age=86400; path=/; domain=.guazi.com, preTime=%7B%22last%22%3A1608638500%2C%22this%22%3A1608638500%2C%22pre%22%3A1608638500%7D; expires=Sat, 20-Feb-2021 12:01:40 GMT; Max-Age=5184000; path=/; domain=.guazi.com
		String domain = domainFromUrl(url);
		Matcher setcookie_name_value_matcher = SETCOOKIE_NAME_VALUE_MATCHER.matcher(setCookie.trim());
		while (setcookie_name_value_matcher.find()) {
			String name = setcookie_name_value_matcher.group(1);
			if (name.equalsIgnoreCase("path") || name.equalsIgnoreCase("domain") || name.equalsIgnoreCase("expires") || name.equalsIgnoreCase("Max-Age")) {
				continue;
			}
			String value = setcookie_name_value_matcher.group(2);
			Cookie cookie = new Cookie(name, value);
			cookie.setDomain(domain);
			catchCookies.put(name, cookie);
		}
	}
	
}
