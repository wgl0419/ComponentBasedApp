package com.hansion.commonlib.utils;

import android.util.Log;

import com.hansion.commonlib.BaseApplication;

/**
 * Description：Log工具类
 * Author: Hansion
 * Time: 2017/2/3 11:11
 */
public class LogUtil {

    private static final boolean isDebug = BaseApplication.Companion.isDebug();
    private static String className;//类名
    private static String methodName;//方法名
    private static int lineNumber;//行数

    private LogUtil(){

    }

    private static boolean isDebuggable() {
        return isDebug;
    }

    private static String createLog( String log ) {
        String buffer = methodName +
                "(" + className + ":" + lineNumber + ")" +
                log;
        return buffer;
    }

    private static void getMethodNames(StackTraceElement[] sElements){
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message){
        if (!isDebuggable())
            return;

        // Throwable instance must be created before any methods
        getMethodNames(new Throwable().getStackTrace());
        Log.e(className, createLog(message));
    }


    public static void i(String message){
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(className, createLog(message));
    }

    public static void d(String message){
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }

    public static void v(String message){
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.v(className, createLog(message));
    }

    public static void w(String message){
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.w(className, createLog(message));
    }
}
