/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.complicationsdataprovider;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.complications.ProviderUpdateRequester;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.ReadableInstant;

/**
 * Example watch face complication data provider provides a number that can be incremented on tap.
 */
public class CustomComplicationProviderService extends ComplicationProviderService {

    private static final String TAG = "ComplicationProvider";

    /*
     * Called when a complication has been activated. The method is for any one-time
     * (per complication) set-up.
     *
     * You can continue sending data for the active complicationId until onComplicationDeactivated()
     * is called.
     */
    @Override
    public void onComplicationActivated(
            int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationActivated(): " + complicationId);
    }
    public static String roomTimetable[][] = {
            {"C2-5", "C2-5", "Break", "D2-4", "D2-4", "D1-5", "Lunch", "A0-3"},
            {"D2-3", "A1-4", "Break", "C2-4", "C2-4", "A0-3", "Lunch", "D2-3"},
            {"A0-3", "D1-5", "Break", "D2-3", "D2-3", "A1-4", "Lunch", "E1-2"},
            {"A1-4", "A1-4", "Break", "A2-3", "A2-3", "D1-5", "Lunch", "D1-5"},
            {"A2-3", "A2-3", "Break", "A1-4", "A1-4", "A0-3", "Lunch", "A0-3"}
    };
    public static String timings[] = {
            "9:15", "10:05", "10:55", "11:10", "12:00", "12:50", "13:40", "14:05", "15:00"
    };
    public static int compareTime(String a, String b) {
        /**
         * Returns 1 if a is later than B
         * If a is earlier than b returns -1
         */
        String[] aParts = a.split(":");
        String[] bParts = b.split(":");

        int aHour = Integer.parseInt(aParts[0]);
        int aMin = Integer.parseInt(aParts[1]);
        int bHour = Integer.parseInt(bParts[0]);
        int bMin = Integer.parseInt(bParts[1]);

        if (aHour < bHour) return -1;
        if (aHour > bHour) return 1;
        if (aMin == bMin) return 0;
        return (aMin >= bMin) ? 1 : -1;

    }
    public static int whatPeriod(String time) {
        for (int i = 0; i < timings.length; i++) {
            if (compareTime(time, timings[i]) == -1) {
                return i;
            }
        }
        return -1;
    }
    public static String getNextPeriodFromTime(String time, int zeroIndexDay) {
        int period = whatPeriod(time);
        if (period == -1) return "X";
        if (period == 8) return "X";
        String lesson = roomTimetable[zeroIndexDay][period];
        return lesson;
    }
    @SuppressWarnings("deprecation")
    public static String getTheString() {
        Date now = new Date();
        int hrs = now.getHours() + 1;
        int mins = now.getMinutes() + 1;
        int day = now.getDay() - 1;
        if (day == -1 || day == 5) return "X"; // If it's a weekend return X
        if (hrs >= 15) return "X";
        if (hrs < 8) return "X";
        if ((hrs == 8) && (mins < 30)) return "X";
        return getNextPeriodFromTime(hrs + ":" + mins, day);
    }

    @Override
    public void onComplicationUpdate(
            int complicationId, int dataType, ComplicationManager complicationManager) {

        Log.d(TAG, "onComplicationUpdate() id: " + complicationId);

        String numberText = getTheString();
        //int period = getPeriod();
        ComplicationData complicationData = null;

        switch (dataType) {
            case ComplicationData.TYPE_SHORT_TEXT:
                complicationData =
                        new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                                .setShortText(ComplicationText.plainText(numberText))
                                .setShortTitle(ComplicationText.plainText("Next"))
                                .build();
                break;
            default:
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type " + dataType);
                }
        }

        if (complicationData != null) {
            complicationManager.updateComplicationData(complicationId, complicationData);

        } else {
            // If no data is sent, we still need to inform the ComplicationManager, so the update
            // job can finish and the wake lock isn't held any longer than necessary.
            complicationManager.noUpdateRequired(complicationId);
        }
    }

    /*
     * Called when the complication has been deactivated.
     */
    @Override
    public void onComplicationDeactivated(int complicationId) {
        Log.d(TAG, "onComplicationDeactivated(): " + complicationId);
    }
}
