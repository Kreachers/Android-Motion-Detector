package com.dealfaro.luca.serviceexample;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;


public class MyService extends Service {
    PowerManager.WakeLock wakeLock;
    private static final String LOG_TAG = "MyService";
    // Handle to notification manager.
    private NotificationManager notificationManager;

    // Motion detector thread and runnable.
    private Thread myThread;
    private MyServiceTask myTask;

    // Binder given to clients
    private final IBinder myBinder = new MyBinder();

    // Binder class.
    class MyBinder extends Binder {
        MyService getService() {
            // Returns the underlying service.
            return MyService.this;
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "Service is being created");

        // Display a notification about us starting.  We put an icon in the status bar.
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        showMyNotification();

        // Creates the thread running the camera service.
        myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
        myThread.start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "Service is being bound");
        // Returns the binder to this service.
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire(15*60*1000L /*15 minutes*/);


        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We start the task thread.
        if (!myThread.isAlive()) {
            myThread.start();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        // Cancel the persistent notification.
        int ONGOING_NOTIFICATION_ID = 1;
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
        Log.i(LOG_TAG, "Stopping.");
        // Stops the motion detector.
        myTask.stopProcessing();
        Log.i(LOG_TAG, "Stopped.");
    }

    // Interface which sets recording on/off.
    public void setTaskState(boolean b) {
        myTask.setTaskState(b);
    }

//
//    /**
//     * Show a notification while this service is running.
//     */
//    @SuppressWarnings("deprecation")
//    private void showMyNotification() {
//        // Creates a notification.
//        Notification notification = new Notification(
//                R.mipmap.ic_launcher,
//                getString(R.string.my_service_started),
//                System.currentTimeMillis());
//
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(this, getText(R.string.notification_title),
//                getText(R.string.my_service_running), pendingIntent);
//        startForeground(ONGOING_NOTIFICATION_ID, notification);
//    }
}
