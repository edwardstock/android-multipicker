package com.edwardstock.multipicker.internal.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;

import timber.log.Timber;

/**
 * Dogsy. 2017
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class ExceptionHelper {

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static void doubleTryOOM(Call action) {
        doubleTryOOM(action, "<unknown>");
    }

    public static void doubleTryOOM(Call action, String doActionName) {
        doubleTryOOM(action, null, doActionName);
    }

    public static void doubleTryOOM(Call action, ErrorCall<OutOfMemoryError> onError, String doActionName) {
        try {
            action.call();
        } catch (OutOfMemoryError e) {
            System.gc();
            try {
                action.call();
            } catch (OutOfMemoryError e2) {
                Timber.w(e2, "Not enough memory (%s): %s", e2.getMessage(), doActionName);
                if (onError != null) {
                    try {
                        onError.call(e2);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public interface Call {
        void call();
    }

    public interface ErrorCall<T> {
        void call(T t);
    }
}
