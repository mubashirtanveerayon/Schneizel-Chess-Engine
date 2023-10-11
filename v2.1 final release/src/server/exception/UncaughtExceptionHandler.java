package server.exception;

import server.util.Util;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Util.initLogger();
        Util.writeToLog("Exception in: "+t.getName());
        Util.writeToLog((Exception)e);
        System.out.println("Exiting engine program due to internal error. See log file.");
        System.exit(0);
    }
}
