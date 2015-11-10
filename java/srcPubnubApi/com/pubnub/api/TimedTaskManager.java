package com.pubnub.api;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

abstract class TimedTask {
    protected int interval;
    public abstract void run();
    TimedTask(int interval) {
        this.setInterval(interval);
    }
    public int getInterval() {
        return interval;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }

}

class TimedTaskWorker implements Runnable{

    private TimedTask task;
    private Thread thread;
    protected volatile boolean _die;
    private String name;
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
    public TimedTask getTask() {
        return task;
    }

    public void setTask(TimedTask task) {
        this.task = task;
    }

    public int getInterval() {
        return task.getInterval();
    }
    public void setInterval(int interval) {
        this.task.setInterval(interval);
    }

    TimedTaskWorker(String name, TimedTask task) {
        this.task = task;
        this.name = name;
    }

    void die() {
        _die = true;
    }

    public void run() {
        do {
            task.run();
            try {
                Thread.sleep(task.getInterval() * 1000);
            } catch (InterruptedException e) {

            }
        } while (!_die);
    }

}


public class TimedTaskManager {

    protected Vector _workers = new Vector();;
    private static int count = 0;
    private TimedTask voidTask = new TimedTask(1) {
        public void run() {}
    };

    protected static Logger log = new Logger(Worker.class);

    public TimedTaskManager(String name) {

    }
    private void interruptWorkers() {
        synchronized(_workers) {
            for (int i = 0; i < _workers.size(); i++) {
                ((TimedTaskWorker)_workers.elementAt(i)).interruptWorker();
            }
        }
    }

    public int addTask(String name, TimedTask task) {
        TimedTaskWorker w = new TimedTaskWorker(name, task);
        Thread thread = new Thread(w, name + "-" + ++count);
        thread.setDaemon(true);
        w.setThread(thread);
        _workers.add(w);
        log.verbose("Starting new worker " + w.getThread().getName());
        w.startWorker();
        return w.hashCode();
    }

    public void removeTask(int hashCode) {
        synchronized(_workers) {
            for (int i = 0; i < _workers.size(); i++) {
                TimedTaskWorker ttw = ((TimedTaskWorker)_workers.elementAt(i));
                if (ttw.hashCode() == hashCode) {
                    ttw.setTask(voidTask);
                    ttw.die();
                    ttw.interruptWorker();
                    _workers.remove(ttw);
                }
            }
        }
    }
    public void updateTask(int hashCode, TimedTask task) {
        synchronized(_workers) {
            for (int i = 0; i < _workers.size(); i++) {
                TimedTaskWorker ttw = ((TimedTaskWorker)_workers.elementAt(i));
                if (ttw.hashCode() == hashCode) {
                    ttw.setTask(task);
                    ttw.interruptWorker();
                }
            }
        }
    }
    public void updateTask(int hashCode, int interval) {
        synchronized(_workers) {
            for (int i = 0; i < _workers.size(); i++) {
                TimedTaskWorker ttw = ((TimedTaskWorker)_workers.elementAt(i));
                if (ttw.hashCode() == hashCode) {
                    ttw.getTask().setInterval(interval);
                    ttw.interruptWorker();
                }
            }
        }
    }
    public void stop() {
        synchronized(_workers) {
            for (int i = 0; i < _workers.size(); i++) {
                TimedTaskWorker ttw = ((TimedTaskWorker)_workers.elementAt(i));
                ttw.setTask(voidTask);
                ttw.die();
                ttw.interruptWorker();
                _workers.remove(ttw);
            }
        }
    }

}
