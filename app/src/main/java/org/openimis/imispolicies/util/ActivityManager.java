package org.openimis.imispolicies.util;

import android.app.Activity;

import java.util.Stack;

public class ActivityManager {

    private static Stack<Activity> activityStack = new Stack<>();

    public static void addActivity(Activity activity) {
        activityStack.push(activity);
    }

    public static void finishCurrentAndPrevious() {
        if (!activityStack.isEmpty()) {
            Activity current = activityStack.pop();
            current.finish();
        }

        if (!activityStack.isEmpty()) {
            Activity previous = activityStack.pop();
            previous.finish();
        }
    }
}
