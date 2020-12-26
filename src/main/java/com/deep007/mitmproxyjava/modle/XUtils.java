package com.deep007.mitmproxyjava.modle;

public class XUtils {

	public static String getContentType(FlowResponse flowResponse) {
		if (flowResponse.getHeader("content-type") != null) {
			return flowResponse.getHeader("content-type");
		}
		if (flowResponse.getHeader("Content-Type") != null) {
			return flowResponse.getHeader("Content-Type");
		}
		return "";
	}
	
	
	public static String getContentType(FlowRequest request) {
		if (request.getHeader("content-type") != null) {
			return request.getHeader("content-type");
		}
		if (request.getHeader("Content-Type") != null) {
			return request.getHeader("Content-Type");
		}
		return "";
	}
	
	public static boolean isJavaScript(FlowResponse flowResponse) {
		String contentType = getContentType(flowResponse);
		if (contentType.contains("/javascript") || contentType.contains("/x-javascript")) {
			return true;
		}
		if (flowResponse.getRequest().getUrl().endsWith(".js")) {
			return true;
		}
		return false;
	}
	
	public static boolean isHtml(FlowResponse flowResponse) {
		String contentType = getContentType(flowResponse);
		if (contentType.contains("/html") || contentType.contains("/x-html")) {
			return true;
		}
		if (flowResponse.getRequest().getUrl().endsWith(".html") || flowResponse.getRequest().getUrl().endsWith(".htm")) {
			return true;
		}
		return false;
	}
	
	public static boolean isImage(FlowResponse flowResponse) {
		FlowRequest request = flowResponse.getRequest();
		if (request.getHeader("Accept") != null && request.getHeader("Accept").startsWith("image/")) {
			return true;
		}
		String contentType = getContentType(flowResponse);
		if (contentType.contains("image/") || contentType.contains("image/")) {
			return true;
		}
		return false;
	}
}
