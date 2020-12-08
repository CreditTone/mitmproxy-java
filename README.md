# 欢迎使用mitmproxy-java

[mitmproxy](https://github.com/mitmproxy/mitmproxy)作为一款出色中间人攻击工具，它在渗透、爬虫、ajax-hook、抓包等场景中表现的相当稳定和出色。但由于原生项目是python的缘故，使得跨语言使用mitmproxy显的非常吃力。经常借助于中间件或单独开发http服务来于mitmproxy进行通信。为此mitmproxy-java基于[mitmproxy-hub](https://github.com/CreditTone/mitmproxy-hub "mitmproxy-hub")实现了java版mitmproxy客户端。你可以像使用原生[mitmproxy](https://github.com/mitmproxy/mitmproxy)一样使用它。

### 原理介绍
[mitmproxy-hub](https://github.com/CreditTone/mitmproxy-hub "mitmproxy-hub")定义了其他任何语言可以生成的proto3序列化代码，借助于grpc高效的跨进程通信。使得其他语言可以对mitmproxy内部的流量进行无死角的监控。

![mitmproxy-hub架构图](https://opensourcefile.oss-cn-beijing.aliyuncs.com/mitmproxy-hub.png "mitmproxy-hub架构图")




### 推荐环境
```
Mitmproxy: 5.3.0
Python:    3.6.8
OpenSSL:   OpenSSL 1.1.1h  22 Sep 2020
Platform:  Darwin-20.1.0-x86_64-i386-64bit
```

### 启动mitmproxy-hub
```
git clone https://github.com/CreditTone/mitmproxy-hub.git
cd mitmproxy-hub
python3 server.py //server on port 60051

```

### 启动一个remote mitmproxy
```java
public static void main(String[] args) throws InterruptedException {
		//在远程机器上的8866端口上启动一个mitmproxy
		RemoteMitmproxy remoteMitmproxy = new RemoteMitmproxy("127.0.0.1", 60051, "127.0.0.1", 8866);
		remoteMitmproxy.start();
		Thread.sleep(1000 * 60 * 5);
		remoteMitmproxy.stop();
}
```

### 监控网络流量
```java
public static void main(String[] args) throws InterruptedException {
		RemoteMitmproxy remoteMitmproxy = new RemoteMitmproxy("127.0.0.1", 60051, "127.0.0.1", 8866);
		remoteMitmproxy.addFlowFilter(new FlowFilter() {
			
			@Override
			public void filterRequest(FlowRequest flowRequest) {
				System.out.println(flowRequest.getUrl());
			}
			
			@Override
			public void filterResponse(FlowResponse flowResponse) {
				FlowRequest flowRequest = flowResponse.getRequest();
				System.out.println(flowRequest.getUrl() + " response length:" +flowResponse.getContent().length);
			}
			
		});
		remoteMitmproxy.start();
		Thread.sleep(1000 * 60 * 5);
		remoteMitmproxy.stop();
}
```

### 篡改网络响应
```java
public static void main(String[] args) throws InterruptedException {
		RemoteMitmproxy remoteMitmproxy = new RemoteMitmproxy("127.0.0.1", 60051, "127.0.0.1", 8866);
		remoteMitmproxy.addFlowFilter(new FlowFilter() {
			
			@Override
			public void filterRequest(FlowRequest flowRequest) {
			}
			
			@Override
			public void filterResponse(FlowResponse flowResponse) {
				FlowRequest flowRequest = flowResponse.getRequest();
				if (flowRequest.getUrl().startsWith("https://www.baidu.com")) {
					flowResponse.setContentAsString("就不让你访问百度，哈哈!");
				}
			}
			
		});
		remoteMitmproxy.start();
		Thread.sleep(1000 * 60 * 5);
		remoteMitmproxy.stop();
}
```


### 劫持cookie
```java
public static void main(String[] args) throws InterruptedException {
		RemoteMitmproxy remoteMitmproxy = new RemoteMitmproxy("127.0.0.1", 60051, "127.0.0.1", 8866);
		CookieCollectFilter cookieCollectFilter = new CookieCollectFilter();
		remoteMitmproxy.addFlowFilter(cookieCollectFilter);
		remoteMitmproxy.start();
	    Thread.sleep(1000 * 60 * 5);
	    remoteMitmproxy.stop();
	    for (Cookie cookie : cookieCollectFilter.catchCookies) {
	    	System.out.println(cookie.getDomain() + ">>>"+ cookie.getName()+"="+cookie.getValue() +" path:"+cookie.getPath());
	    }
}
```

#### 有问题请留言谢谢！

