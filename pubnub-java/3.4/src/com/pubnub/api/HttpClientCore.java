package com.pubnub.api;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import com.pubnub.api.PubnubException;

class HttpClientCore extends HttpClient {
    private int requestTimeout = 310000;
    private int connectionTimeout = 5000;
    HttpURLConnection connection;

    private void init() {
        HttpURLConnection.setFollowRedirects(true);
    }

    public HttpClientCore(int connectionTimeout, int requestTimeout, Hashtable headers) {
        init();
        this.setRequestTimeout(requestTimeout);
        this.setConnectionTimeout(connectionTimeout);
        this._headers = headers;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public boolean isRedirect(int rc) {
        return (rc == HttpURLConnection.HTTP_MOVED_PERM
                || rc == HttpURLConnection.HTTP_MOVED_TEMP || rc == HttpURLConnection.HTTP_SEE_OTHER);
    }

    public boolean checkResponse(int rc) {
        return (rc == HttpURLConnection.HTTP_OK || isRedirect(rc));
    }

    public boolean checkResponseSuccess(int rc) {
        return (rc == HttpURLConnection.HTTP_OK);
    }

    private static String readInput(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte bytes[] = new byte[1024];

        int n = in.read(bytes);

        while (n != -1) {
            out.write(bytes, 0, n);
            n = in.read(bytes);
        }

        return new String(out.toString());
    }

    public HttpResponse fetch(String url) throws IOException, PubnubException {
        return fetch(url, null);
    }

    public synchronized HttpResponse fetch(String url, Hashtable headers)
            throws IOException, PubnubException {
        URL urlobj = new URL(url);
        connection = (HttpURLConnection) urlobj.openConnection();
        connection.setRequestMethod("GET");
        if (_headers != null) {
            Enumeration en = _headers.keys();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String val = (String) _headers.get(key);
                connection.addRequestProperty(key, val);
            }
        }
        if (headers != null) {
            Enumeration en = headers.keys();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String val = (String) headers.get(key);
                connection.addRequestProperty(key, val);
            }
        }
        connection.setReadTimeout(requestTimeout);
        connection.setConnectTimeout(connectionTimeout);
        connection.connect();
        InputStream is;
        if(connection.getContentEncoding() == null || !connection.getContentEncoding().equals("gzip")) {
        	is = connection.getInputStream();
        	
        } else {
        	is = new GZIPInputStream(connection.getInputStream());
        }
        String page = readInput(is);
        return new HttpResponse(connection.getResponseCode(), page);
    }

    public boolean isOk(int rc) {
        return (rc == HttpURLConnection.HTTP_OK);
    }

    public void shutdown() {
        connection.disconnect();
    }
}
