package com.eyougo.rollcrawler.util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


public class HttpClientUtil {
    private static HttpClient httpClient;
    private static PoolingHttpClientConnectionManager connectionManager;

    /**
     * 最大连接数
     */
    public final static int MAX_TOTAL_CONNECTIONS = 500;
    /**
     * 每个路由最大连接数
     */
    public final static int MAX_ROUTE_CONNECTIONS = 200;
    /**
     * 连接超时时间
     */
    public final static int CONNECT_TIMEOUT = 3000;
    /**
     * 读取超时时间
     */
    public final static int READ_TIMEOUT = 5000;

    static {
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(READ_TIMEOUT).build();
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).build();

        httpClient = HttpClients.custom().setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).build();

    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}
