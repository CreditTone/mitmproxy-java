package com.deep007.mitmproxyjava.mitmproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.deep007.mitmproxyjava.MitmproxyStartRequest;
import com.deep007.mitmproxyjava.MitmproxyStartResponse;
import com.deep007.mitmproxyjava.MitmproxyStopRequest;
import com.deep007.mitmproxyjava.VoidResponse;
import com.deep007.mitmproxyjava.filter.Cookie;
import com.deep007.mitmproxyjava.filter.CookieCollectFilter;
import com.deep007.mitmproxyjava.filter.FlowFilter;
import com.deep007.mitmproxyjava.grpc.MitmProxyHubServerGrpc;
import com.deep007.mitmproxyjava.grpc.MitmproxyFlowCallBackServer;
import com.deep007.mitmproxyjava.modle.FlowRequest;
import com.deep007.mitmproxyjava.modle.FlowResponse;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

public class RemoteMitmproxy {
	
	public static final Map<String,RemoteMitmproxy> RemoteMitmproxies = new ConcurrentHashMap<>();
	
	private String mitmproxyHubAddr;
	
	private int mitmproxyHubPort;
	
	private String remoteBind;
	
	private int remoteBindPort;
	
	private String upstream;
	
	private String upstreamAuth;
	
	private volatile String mitmproxyId;
	
	private MitmProxyHubServerGrpc.MitmProxyHubServerBlockingStub mitmProxyHubServerBlockingStub;
	
	private List<FlowFilter> filters = new ArrayList<>();
	
	/**
	 * 启动一个Remote Mitmproxy 
	 * @param mitmproxyHubAddr mitmproxy-hub服务的ip，不知道mitmproxy-hub是什么请看https://github.com/CreditTone/mitmproxy-hub
	 * @param mitmproxyHubPort mitmproxy-hub服务的端口 
	 * @param remoteBind       在mitmproxy-hub这个机器上启动一个mitmproxy实例，告诉它需要绑定的IP。如果是本机的话绑定127.0.0.1即可，如果不是本机绑定0.0.0.0。并在使用的时候注意本机到mitmproxy-hub服务的IP关系。相信稍微有点网络知识不必我再说了吧
	 * @param remoteBindPort   在mitmproxy-hub这个机器上启动一个mitmproxy实例，告诉它需要绑定的端口
	 */
	public RemoteMitmproxy(String mitmproxyHubAddr, int mitmproxyHubPort, String remoteBind, int remoteBindPort) {
		this(mitmproxyHubAddr, mitmproxyHubPort, remoteBind, remoteBindPort, null, null);
	}
	
	/**
	 * 启动一个Remote Mitmproxy
	 * @param mitmproxyHubAddr 
	 * @param mitmproxyHubPort
	 * @param remoteBind
	 * @param remoteBindPort
	 * @param upstream  启动远端mitmproxy实例的同时设置一个上游的http代理 格式如:http://http-dyn.abuyun.com:9020、http://192.168.0.101:8080。类似命令：mitmdump --mode upstream:http://192.168.0.101:8080，还不了解自己去了解https://docs.mitmproxy.org/archive/v5/concepts-modes/#upstream-proxy
	 */
	public RemoteMitmproxy(String mitmproxyHubAddr, int mitmproxyHubPort, String remoteBind, int remoteBindPort, String upstream) {
		this(mitmproxyHubAddr, mitmproxyHubPort, remoteBind, remoteBindPort, upstream, null);
	}
	
	/**
	 * 例如：RemoteMitmproxy remoteMitmproxy = new RemoteMitmproxy("127.0.0.1", 60051, "127.0.0.1", 8866, "http://http-dyn.abuyun.com:9020", "H889CWY00SVY012D:263445C168FAE095");
	 * 
	 * @param mitmproxyHubAddr
	 * @param mitmproxyHubPort
	 * @param remoteBind
	 * @param remoteBindPort
	 * @param upstream  启动远端mitmproxy实例的同时设置一个上游的http代理 格式如:http://http-dyn.abuyun.com:9020、http://192.168.0.101:8080
	 * @param upstreamAuth  如果上游的http代理有验证，则设置。 格式：H889CWY00SVY012D:263445C168FAE095
	 */
	public RemoteMitmproxy(String mitmproxyHubAddr, int mitmproxyHubPort, String remoteBind, int remoteBindPort, String upstream, String upstreamAuth) {
		this.mitmproxyHubAddr = mitmproxyHubAddr;
		this.mitmproxyHubPort = mitmproxyHubPort;
		this.remoteBind = remoteBind;
		this.remoteBindPort = remoteBindPort;
		this.upstream = upstream;
		this.upstreamAuth = upstreamAuth;
	}
	
	public synchronized void start() {
		if (this.mitmproxyId == null) {
			MitmproxyFlowCallBackServer mitmproxyFlowCallBackServer = MitmproxyFlowCallBackServer.getInstance();
			MitmproxyStartRequest.Builder builder = MitmproxyStartRequest.newBuilder()
					.setBind(remoteBind)
					.setPort(remoteBindPort)
					.setCallbackServerAddr("127.0.0.1")
					.setCallbackServerPort(mitmproxyFlowCallBackServer.port);
			if (this.upstream != null) {
				builder.setUpstream(upstream);
			}
			if (this.upstreamAuth != null) {
				builder.setUpstreamAuth(upstreamAuth);
			}
			Channel channel = ManagedChannelBuilder.forAddress(mitmproxyHubAddr, mitmproxyHubPort).usePlaintext().build();
			this.mitmProxyHubServerBlockingStub = MitmProxyHubServerGrpc.newBlockingStub(channel);
			MitmproxyStartResponse response = mitmProxyHubServerBlockingStub.start(builder.build());
			this.mitmproxyId = response.getMitmproxyId();
			RemoteMitmproxies.put(mitmproxyId, this);
		}
	}
	
	
	public void addFlowFilter(FlowFilter flowFilter) {
		filters.add(flowFilter);
	}
	
	public boolean isRunning() {
		return mitmproxyId != null;
	}
	
	public void stop() {
		if (isRunning()) {
			try {
				MitmproxyStopRequest request = MitmproxyStopRequest.newBuilder().setMitmproxyId(mitmproxyId).build();
				VoidResponse response = this.mitmProxyHubServerBlockingStub.stop(request);
			} catch (Exception e) {
				//e.printStackTrace();
			}
			RemoteMitmproxies.remove(mitmproxyId);
		}
	}
	
	public void onRequest(FlowRequest flowRequest) {
		for (FlowFilter flowFilter : filters) {
			try {
				flowFilter.filterRequest(flowRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onResponse(FlowResponse flowResponse) {
		for (FlowFilter flowFilter : filters) {
			try {
				flowFilter.filterResponse(flowResponse);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	public String getRemoteBind() {
		return remoteBind;
	}

	public int getRemoteBindPort() {
		return remoteBindPort;
	}

	public static void main(String[] args) throws InterruptedException {
		RemoteMitmproxy remoteMitmproxy = new RemoteMitmproxy("127.0.0.1", 60051, "127.0.0.1", 8866, "http://http-dyn.abuyun.com:9020", "H889CWY00SVY012D:263445C168FAE095");
		CookieCollectFilter cookieCollectFilter = new CookieCollectFilter();
		remoteMitmproxy.addFlowFilter(cookieCollectFilter);
		remoteMitmproxy.addFlowFilter(new FlowFilter() {
			
			@Override
			public void filterResponse(FlowResponse flowResponse) {
				flowResponse.getHeaders().remove("Server");
			}
			
			@Override
			public void filterRequest(FlowRequest flowRequest) {
				flowRequest.getHeaders().remove("User-Agent");
				flowRequest.getHeaders().remove("Accept");
			}
		});
		remoteMitmproxy.start();
	    Thread.sleep(1000 * 30);
	    remoteMitmproxy.stop();
	    for (Cookie cookie : cookieCollectFilter.catchCookies.values()) {
	    	System.out.println(cookie.getDomain() + ">>>"+ cookie.getName()+"="+cookie.getValue() +" path:"+cookie.getPath());
	    }
	}

}
