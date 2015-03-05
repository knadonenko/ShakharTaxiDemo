package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class RepeatingCheckService extends BroadcastReceiver {

	JSONParser jsonParser = new JSONParser();

	AlertDialog alertDialog;

	JSONArray json;

	String order;
	Boolean canceled = false;

	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String ACTION_ID = "checkCancel";

	public void onReceive(Context context, Intent intent) {
		Log.d("CHECK ORDER", "STATUS");
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		sp.getString("order", "no order");
		new checkOrderStatus().execute();
		if (canceled) {
			Toast.makeText(context, "ЗАКАЗ ОТМЕНЕН!!!", Toast.LENGTH_LONG)
					.show();
			try {
				Uri notification = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone r = RingtoneManager.getRingtone(context, notification);
				r.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*
		 * if (!canceled) {
		 * 
		 * 
		 * }
		 */

	}

	class checkOrderStatus extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_ID));
			params.add(new BasicNameValuePair("order", order));

			Log.d("request!", "starting");

			// отправляем JSON
			json = jsonParser.makeArrayRequest(LOGIN_URL, "POST", params);

			// полный ответ от json
			Log.d("CHECK STATUS", json.toString());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try {
				for (int i = 0; i < json.length(); i++) {
					JSONObject c = json.getJSONObject(i);
					String status = c.getString("cancel");
					if ("1".equals(status)) {
						canceled = true;
					}

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
