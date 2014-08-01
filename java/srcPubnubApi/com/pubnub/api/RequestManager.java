package com.pubnub.api;

import java.util.Hashtable;
import java.util.Vector;

abstract class Worker implements Runnable {
    private Vector _requestQueue;
    protected volatile boolean _die;
    private Thread thread;
    protected HttpClient httpclient;

    protected static Logger log = new Logger(Worker.class);

    public Thread getThread() {
        return thread;
    }

    void setThread(Thread thread) {
        this.thread = thread;
    }

    void startWorker() {
        thread.start();
    }

    void interruptWorker() {
        thread.interrupt();
    }

    void resetConnection() {
        httpclient.reset();
    }

    Worker(Vector _requestQueue, int connectionTimeout, int requestTimeout, Hashtable headers) {
        this._requestQueue = _requestQueue;
        this.httpclient = HttpClient.getClient(connectionTimeout,
                                               requestTimeout, headers);
    }

    void setConnectionTimeout(int timeout) {
        if (httpclient != null) {
            httpclient.setConnectionTimeout(timeout);
        }
    }

    void setRequestTimeout(int timeout) {
        if (httpclient != null) {
            httpclient.setRequestTimeout(timeout);
        }
    }

    public abstract void shutdown();

    void die() {
        _die = true;
    }

    abstract void process(HttpRequest hreq);

    public void run() {
        do {
            HttpRequest hreq = null;
            while (!_die) {

                synchronized (_requestQueue) {

                    if (_requestQueue.size() != 0) {
                        hreq = (HttpRequest) _requestQueue.firstElement();
                        _requestQueue.removeElementAt(0);
                        break;
                    }
                    try {
                        _requestQueue.wait(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (hreq != null) {
                if (!_die) {
                    process(hreq);
                }
            }
        } while (!_die);
        shutdown();
    }
}

class NonSubscribeWorker extends Worker {

    NonSubscribeWorker(Vector _requestQueue, int connectionTimeout,
                       int requestTimeout, Hashtable headers) {
        super(_requestQueue, connectionTimeout, requestTimeout, headers);
    }

    void process(HttpRequest hreq) {
        HttpResponse hresp = null;
        try {
            log.debug(hreq.getUrl());
            hresp = httpclient.fetch(hreq.getUrl(), hreq.getHeaders());
        } catch (PubnubException pe) {
            log.debug("Pubnub Exception in Fetch : " + pe.getPubnubError());
            if (!_die)
                hreq.getResponseHandler().handleError(hreq, pe.getPubnubError());
            return;
        } catch (Exception e) {
            log.debug("Exception in Fetch : " + e.toString());
            if (!_die)
                hreq.getResponseHandler().handleError(hreq, PubnubError.getErrorObject(PubnubError.PNERROBJ_HTTP_ERROR, 2, e.toString()));
            return;
        }

        if (!_die) {
            if (hresp == null) {
                log.debug("Error in fetching url : " + hreq.getUrl());
                hreq.getResponseHandler().handleError(hreq, PubnubError.getErrorObject(PubnubError.PNERROBJ_HTTP_ERROR , 3));
                return;
            }
            hreq.getResponseHandler().handleResponse(hreq, hresp.getResponse());
        }
    }

    public void shutdown() {
        if (httpclient != null) httpclient.shutdown();
    }

}

abstract class RequestManager {

    private static int _maxWorkers = 1;
    protected Vector _waiting = new Vector();
    protected Worker _workers[];
    protected String name;
    protected volatile int connectionTimeout;
    protected volatile int requestTimeout;
    protected Hashtable headers;
    private static int count = 0;

    protected static Logger log = new Logger(RequestManager.class);

    public static int getWorkerCount() {
        return _maxWorkers;
    }

    public abstract Worker getWorker();

    private void initManager(int maxCalls, String name) {
        if (maxCalls < 1) {
            maxCalls = 1;
        }
        this.name = name;
        this.headers = new Hashtable();
        _workers = new Worker[maxCalls];

        synchronized (_workers) {
            for (int i = 0; i < maxCalls; ++i) {
                Worker w = getWorker();
                w.setThread(new Thread(w, name + "-" + ++count));
                _workers[i] = w;
                log.verbose("Starting new worker " + _workers[i].getThread().getName());
                w.startWorker();
            }
        }
    }

    public RequestManager(String name, int connectionTimeout, int requestTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
        initManager(_maxWorkers, name);
    }

    private void interruptWorkers() {
        synchronized (_workers) {
            for (int i = 0; i < _workers.length; i++) {
                _workers[i].interruptWorker();
            }
        }
    }

    class ConnectionResetter implements Runnable {
        Worker worker;
        ConnectionResetter(Worker w) {
            this.worker = w;
        }
        public void run() {
            if (this.worker != null) {
                worker.resetConnection();
            }
        }
    }

    public void resetWorkers() {
        synchronized (_workers) {
            for (int i = 0; i < _workers.length; i++) {
                log.verbose("Sending DIE to " + _workers[i].getThread().getName());
                _workers[i].die();
                new Thread(new ConnectionResetter(_workers[i])).start();
                _workers[i].interruptWorker();
                Worker w = getWorker();
                w.setThread(new Thread(w, name + "-" + ++count));
                _workers[i] = w;
                log.verbose("Starting new worker " + _workers[i].getThread().getName());
                w.startWorker();
            }
        }
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public abstract void clearRequestQueue();

    public void resetHttpManager() {
        clearRequestQueue();
        resetWorkers();
    }

    public void abortClearAndQueue(HttpRequest hreq) {
        resetHttpManager();
        queue(hreq);
    }

    public void queue(HttpRequest hreq) {
        log.debug("Queued : " + hreq.getUrl());
        synchronized (_waiting) {
            _waiting.addElement(hreq);
            _waiting.notifyAll();
        }
    }

    public static void setWorkerCount(int count) {
        _maxWorkers = count;
    }

    public void stop() {
        synchronized (_workers) {
            for (int i = 0; i < _maxWorkers; ++i) {
                Worker w = _workers[i];
                w.die();
            }
        }
        synchronized (_waiting) {
            _waiting.notifyAll();
        }
    }
}

abstract class AbstractSubscribeManager extends RequestManager {

    protected volatile int maxRetries = 5;
    protected volatile int retryInterval = 5000;
    protected volatile int windowInterval = 0;

    public AbstractSubscribeManager(String name, int connectionTimeout,
                                    int requestTimeout) {
        super(name, connectionTimeout, requestTimeout);
    }

    public Worker getWorker() {
        return new SubscribeWorker(_waiting,
                                   connectionTimeout, requestTimeout,
                                   maxRetries, retryInterval, windowInterval, headers);
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        for (int i = 0; i < _workers.length; i++) {
            ((SubscribeWorker) _workers[i]).setMaxRetries(maxRetries);
        }
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        for (int i = 0; i < _workers.length; i++) {
            ((SubscribeWorker) _workers[i]).setRetryInterval(retryInterval);
        }
    }

    public void setWindowInterval(int windowInterval) {
        this.windowInterval = windowInterval;
        for (int i = 0; i < _workers.length; i++) {
            ((SubscribeWorker) _workers[i]).setWindowInterval(windowInterval);
        }
    }

    public void setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
    }

    public void setRequestTimeout(int timeout) {
        this.requestTimeout = timeout;
    }

    public void queue(HttpRequest hreq) {
        synchronized (_waiting) {
            clearRequestQueue();
            super.queue(hreq);
        }
    }
}

abstract class AbstractNonSubscribeManager extends RequestManager {
    public AbstractNonSubscribeManager(String name, int connectionTimeout,
                                       int requestTimeout) {
        super(name, connectionTimeout, requestTimeout);
    }

    public Worker getWorker() {
        return new NonSubscribeWorker(_waiting, connectionTimeout,
                                      requestTimeout, headers);
    }

    public void setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
        for (int i = 0; i < _workers.length; i++) {
            _workers[i].setConnectionTimeout(timeout);
        }
    }

    public void setRequestTimeout(int timeout) {
        this.requestTimeout = timeout;
        for (int i = 0; i < _workers.length; i++) {
            _workers[i].setRequestTimeout(timeout);
        }
    }

}

abstract class AbstractSubscribeWorker extends Worker {
    protected volatile int maxRetries = 5;
    protected volatile int retryInterval = 5000;
    protected volatile int windowInterval = 0;

    AbstractSubscribeWorker(Vector _requestQueue, int connectionTimeout,
                            int requestTimeout, int maxRetries, int retryInterval, Hashtable headers) {
        super(_requestQueue, connectionTimeout, requestTimeout, headers);
        this.maxRetries = maxRetries;
        this.retryInterval= retryInterval;
    }

    AbstractSubscribeWorker(Vector _requestQueue, int connectionTimeout,
            int requestTimeout, int maxRetries, int retryInterval, int windowInterval, Hashtable headers) {
        super(_requestQueue, connectionTimeout, requestTimeout, headers);
        this.maxRetries = maxRetries;
        this.retryInterval= retryInterval;
        this.windowInterval = windowInterval;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public void setWindowInterval(int windowInterval) {
        this.windowInterval = windowInterval;
    }

}
