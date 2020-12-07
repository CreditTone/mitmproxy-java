# 欢迎使用mitmproxy-java

mitmproxy-java是基于mitmproxy-hub实现的java版客户端。你可以像使用原生mitmproxy一样使用它。

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

#### 有问题请留言谢谢！

