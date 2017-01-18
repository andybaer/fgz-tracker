package com.baer.fgztracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.Closeable;
import java.io.IOException;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by andy on 1/17/17
 */

class Utils {

	static void updateOngoingNotification(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String text = prefs.getString(CheckerService.RESULT, "Not checked yet");
		createOngoingNotification(context, text);
	}

	static void scheduleDaily(Context context) {
		Calendar alarmTime = Calendar.getInstance();
		alarmTime.set(Calendar.HOUR_OF_DAY, 16);
		alarmTime.set(Calendar.MINUTE, 0);
		alarmTime.set(Calendar.SECOND, 0);
		if (Calendar.getInstance().after(alarmTime)) {
			alarmTime.add(Calendar.DATE, 1);
		}
		PendingIntent pi = PendingIntent.getService(context, 22,
				new Intent(context, CheckerService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, pi);
	}

	private static void createOngoingNotification(Context context, String text) {
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

		NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
		bigTextStyle.setBigContentTitle(context.getString(R.string.app_name));
		bigTextStyle.bigText(text);

		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_house)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(text)
				.setStyle(bigTextStyle)
				.setPriority(Notification.PRIORITY_MIN).build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		mNotifyMgr.notify(257896, notification);
	}

	static void createAlertNotification(Context context, String text, Intent intent) {
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

		NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
		bigTextStyle.setBigContentTitle(context.getString(R.string.app_name));
		bigTextStyle.bigText(text);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_house)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(text)
				.setStyle(bigTextStyle)
				.setAutoCancel(true)
				.setPriority(Notification.PRIORITY_MAX)
				.setContentIntent(pendingIntent).build();

		notification.defaults = Notification.DEFAULT_ALL;
		mNotifyMgr.notify(98745, notification);
	}

	static void closeQuietly(Closeable stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

}
