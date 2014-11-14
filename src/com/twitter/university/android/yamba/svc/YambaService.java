package com.twitter.university.android.yamba.svc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twitter.university.android.yamba.R;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    private static final String PARAM_OP = "YambaService.OP";
    private static final int OP_POLL = -1;
    private static final int OP_POST = -2;

    private static final String PARAM_TWEET = "YambaService.TWEET";

    private static final int POLL_REQ = 42;

    public static void postTweet(Context ctxt, String tweet) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_OP, OP_POST);
        i.putExtra(PARAM_TWEET, tweet);
        ctxt.startService(i);
    }

    public static void startPolling(Context ctxt) {
        Log.d(TAG, "Polling started");
        long t = 1000 * ctxt.getResources().getInteger(R.integer.poll_interval);
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                t,
                getPollingIntent(ctxt));
    }

    public static void stopPolling(Context ctxt) {
        Log.d(TAG, "Polling stopped");
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .cancel(getPollingIntent(ctxt));
    }

    private static PendingIntent getPollingIntent(Context ctxt) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_OP, OP_POLL);
        return PendingIntent.getService(
            ctxt,
            POLL_REQ,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private volatile YambaLogic helper;

    public YambaService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new YambaLogic(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int op = intent.getIntExtra(PARAM_OP, 0);
        switch(op) {
            case OP_POLL:
                helper.doPoll();
                break;

            case OP_POST:
                helper.doPost(intent.getStringExtra(PARAM_TWEET));
                break;

            default:
                throw new IllegalArgumentException("Unrecognized op: " + op);
        }
    }
}
