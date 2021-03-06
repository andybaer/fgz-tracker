package com.baer.fgztracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andy on 1/19/17
 */
public class CheckerService extends Service {

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM - HH:mm", Locale.getDefault());
	private static final String FAILURE = "FAILURE ";
	private final Scheduler scheduler = new Scheduler();
	private final Notifier notifier = new Notifier();
	private final UserPrefs userPrefs = new UserPrefs();
	private final ContentAnalyzer contentAnalyzer = new ContentAnalyzer();
	private final ContentPreparer contentPreparer = new ContentPreparer();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		runChecker();
		if (intent.hasExtra(Scheduler.RESCHEDULE_FLAG)) {
			scheduler.rescheduleDaily(this);
		}
		return Service.START_NOT_STICKY;
	}

	private void runChecker() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return fetchPage();
			}

			@Override
			protected void onPostExecute(String newContent) {
				super.onPostExecute(newContent);
				Context context = CheckerService.this;
				String prevContent = userPrefs.getSiteContent(context);
				String time = sdf.format(new Date());
				String result;
				if (StringUtils.startsWith(newContent, FAILURE)) {
					result = newContent;
					userPrefs.setTrackingResult(context, newContent);
				} else {
					newContent = contentPreparer.prepare(newContent);
					boolean hasHouse = contentAnalyzer.hasNewHouse(prevContent, newContent);
					if (hasHouse) {
						result = "house found at " + time;
						Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(userPrefs.getTrackingUrl(context)));
						notifier.createAlertNotification(context, "house found, click to view", resultIntent);
					} else {
						result = "no house found " + time;
					}
					userPrefs.setSiteContentAndResult(context, newContent, result);
				}
				userPrefs.setLastRun(context, time);
				notifier.updateOngoingNotification(context, result);
			}
		}.execute();
	}

	private String fetchPage() {
		BufferedReader inputReader = null;
		try {
			String url = userPrefs.getTrackingUrl(this);
			Log.d("CheckerService", "Fetching content from url: " + url);
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Cache-Control", "tmax-age=0");
			conn.connect();

			StringBuilder sb = new StringBuilder();
			inputReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = inputReader.readLine()) != null) {
				sb.append(inputLine);
			}
			conn.disconnect();
			return sb.toString();
		} catch (Exception e) {
			return FAILURE + sdf.format(new Date()) + ": " + e.getMessage();
		} finally {
			try {
				if (inputReader != null) {
					inputReader.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}

}
