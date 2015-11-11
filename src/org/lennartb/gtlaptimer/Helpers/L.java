package org.lennartb.gtlaptimer.Helpers;

import android.util.Log;

/**
 * Wrapper class for improved logging. Originally from http://cleancode.com.ua/?p=767.
 * Automatically displays the class and method from where a message was printed.
 */

public final class L {

    public static void v(final String msg) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();

        String TAG = "(" + callerClassName + ")";
        Log.v(TAG, "(" + callerMethodName + ") " + msg);
    }

    public static void e(final String msg) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();

        String TAG = "(" + callerClassName + ")";
        Log.e(TAG, "(" + callerMethodName + ") " + msg);
    }

    public static void i(final String msg) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();

        String TAG = "(" + callerClassName + ")";
        Log.i(TAG, "(" + callerMethodName + ") " + msg);
    }

    public static void d(final String msg) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();

        String TAG = "(" + callerClassName + ")";
        Log.d(TAG, "(" + callerMethodName + ") " + msg);
    }

    public static void w(final String msg) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();

        String TAG = "(" + callerClassName + ")";
        Log.w(TAG, "(" + callerMethodName + ") " + msg);
    }
}