package me.jaywilliams.timetabler.dataprovider;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import java.util.Date;

public class CustomComplicationProviderService extends ComplicationProviderService {

    private static final String TAG = "ComplicationProvider";
    public static String roomTimetable[][] = {
            {"C2-5", "C2-5", "Break", "D2-3", "A2-3", "D1-5", "Lunch", "D2-4"},
            {"C2-4", "C2-4", "Break", "A0-6", "D2-3", "D2-3", "Lunch", "A1-4"},
            {"D2-3", "D2-3", "Break", "A1-4", "E1-2", "A1-4", "Lunch", "A0-6"},
            {"A0-6", "A1-4", "Break", "A2-3", "A2-3", "D1-5", "Lunch", "D1-5"},
            {"A1-4", "A1-4", "Break", "A0-6", "A0-6", "A2-3", "Lunch", "D1-5"}
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
        int hrs = now.getHours();
        int mins = now.getMinutes();
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
