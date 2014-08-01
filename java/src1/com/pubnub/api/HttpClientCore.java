package com.pubnub.api;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.PubnubException;
import static com.pubnub.api.PubnubError.*;

class HttpClientCore extends HttpClient {
    private int requestTimeout = 310000;
    private int connectionTimeout = 5000;
    HttpURLConnection connection;
    protected static Logger log = new Logger(Worker.class);

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

    public HttpResponse fetch(String url) throws PubnubException, SocketTimeoutException {
        return fetch(url, null);
    }

    public synchronized HttpResponse fetch(String url, Hashtable headers)
    throws PubnubException, SocketTimeoutException {
        URL urlobj = null;
        log.verbose("FETCHING URL : " + url);
        try {
            urlobj = new URL(url);
        } catch (MalformedURLException e3) {
            throw new PubnubException(getErrorObject(PNERROBJ_MALFORMED_URL,url));
        }
        try {
            connection = (HttpURLConnection) urlobj.openConnection();
        } catch (IOException e2) {
            throw new PubnubException(getErrorObject(PNERROBJ_URL_OPEN, url));
        }
        try {
            connection.setRequestMethod("GET");
        } catch (ProtocolException e1) {
            throw new PubnubException(PNERROBJ_PROTOCOL_EXCEPTION);
        }
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

        /*
        try {
            connection.connect();
        } catch (SocketTimeoutException  e) {
            throw e;
        } catch (IOException e) {
            throw new PubnubException(getErrorObject(PNERROBJ_CONNECT_EXCEPTION, url + " : " + e.toString()));
        }
        */
        int rc = HttpURLConnection.HTTP_INTERNAL_ERROR;
        try {
            rc = connection.getResponseCode();
        } catch (SocketTimeoutException ste) {
            throw ste;
        }
        catch (IOException e) {
            throw new PubnubException(getErrorObject(PNERROBJ_HTTP_RC_ERROR, url + " : " + e.toString()));
        }

        InputStream is = null;
        String encoding = connection.getContentEncoding();

        if (encoding == null || !encoding.equals("gzip")) {
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                if (rc == HttpURLConnection.HTTP_OK)
                    throw new PubnubException(getErrorObject(PNERROBJ_GETINPUTSTREAM, 1, url));
                is = connection.getErrorStream();
            }

        } else {
            try {
                is = new GZIPInputStream(connection.getInputStream());
            } catch (IOException e) {
                if (rc == HttpURLConnection.HTTP_OK)
                    throw new PubnubException(getErrorObject(PNERROBJ_GETINPUTSTREAM, 2, url));
                is = connection.getErrorStream();
            }
        }

        String page = null;
        try {
            page = readInput(is);
        } catch (IOException e) {
            throw new PubnubException(getErrorObject(PNERROBJ_READINPUT, url));
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        log.verbose("URL = " + url + ", Status Code : "  + rc + ", : RESPONSE = " + page);
        switch (rc) {
        case HttpURLConnection.HTTP_FORBIDDEN:
            {
                JSONObject payload = null;
                String message = null;
                try {
                    JSONObject pageJso = new JSONObject(page);
                    message            = pageJso.getString("message");
                    payload            = pageJso.getJSONObject("payload");
                    throw new PubnubException(getErrorObject(PNERROBJ_FORBIDDEN, message, payload));
                } catch (JSONException e2) {}

                throw new PubnubException(getErrorObject(PNERROBJ_FORBIDDEN, page));
            }
        case HttpURLConnection.HTTP_UNAUTHORIZED:
            {
                JSONObject payload = null;
                String message = null;
                try {
                    JSONObject pageJso = new JSONObject(page);
                    message            = pageJso.getString("message");
                    payload            = pageJso.getJSONObject("payload");
                    throw new PubnubException(getErrorObject(PNERROBJ_FORBIDDEN, message, payload));
                } catch (JSONException e2) {}

                throw new PubnubException(getErrorObject(PNERROBJ_UNAUTHORIZED, page));
            }
        case HttpURLConnection.HTTP_BAD_REQUEST:
            {
                JSONObject payload = null;
                String message = null;
                try {
                    JSONObject pageJso = new JSONObject(page);
                    message            = pageJso.getString("message");
                    payload            = pageJso.getJSONObject("payload");
                    throw new PubnubException(getErrorObject(PNERROBJ_BAD_REQUEST, message, payload));
                } catch (JSONException e2) {}

                throw new PubnubException(getErrorObject(PNERROBJ_BAD_REQUEST, page));
            }
        case HttpURLConnection.HTTP_NOT_FOUND:
            {
                JSONObject payload = null;
                String message = null;
                try {
                    JSONObject pageJso = new JSONObject(page);
                    message            = pageJso.getString("message");
                    payload            = pageJso.getJSONObject("payload");
                    throw new PubnubException(getErrorObject(PNERROBJ_BAD_REQUEST, message, payload));
                } catch (JSONException e2) {}

                throw new PubnubException(getErrorObject(PNERROBJ_NOT_FOUND_ERROR, page));
            }
        case HttpURLConnection.HTTP_BAD_GATEWAY:
            throw new PubnubException(getErrorObject(PNERROBJ_BAD_GATEWAY, url));
        case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
            throw new PubnubException(getErrorObject(PNERROBJ_CLIENT_TIMEOUT, url));
        case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
            throw new PubnubException(getErrorObject(PNERROBJ_GATEWAY_TIMEOUT, url));
        case HttpURLConnection.HTTP_INTERNAL_ERROR:
            throw new PubnubException(getErrorObject(PNERROBJ_INTERNAL_ERROR, url + " : " + rc));
        default:
            break;
        }
        return new HttpResponse(rc, page);
    }

    public boolean isOk(int rc) {
        return (rc == HttpURLConnection.HTTP_OK);
    }

    public void shutdown() {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
            }
        }
    }
}

