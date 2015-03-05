package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class RepeatingAlarmService extends BroadcastReceiver {

	double begin_lat;
	double begin_long;
	String car_id;

	JSONParser jsonParser = new JSONParser();

	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String ACTION_ID = "setCarLocation";

	@Override
	public void onReceive(Context context, Intent intent) {

		GPSTracker gpsTrack = new GPSTracker(context);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		car_id = sp.getString("car_id", "anon");
		
		Log.d("car_id", car_id);
		

		if (gpsTrack.canGetLocation()) {

			begin_lat = gpsTrack.getLatitude();
			begin_long = gpsTrack.getLongitude();
			Log.d("Lat", Double.toString(begin_lat));
			Log.d("Long", Double.toString(begin_long));

			new SendCoordinates().execute();
		}
	}

	class SendCoordinates extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_ID));
			params.add(new BasicNameValuePair("car_id", car_id));
			params.add(new BasicNameValuePair("ltd", String.format("%.5f",
					begin_lat)));
			params.add(new BasicNameValuePair("lng", String.format("%.5f",
					begin_long)));

			Log.d("request!", "starting");

			// отправляем JSON
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
					params);

			// полный ответ от json
			Log.d("Login attempt", json.toString());
			return null;
		}
	}
}
