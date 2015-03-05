package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseAnalytics;

public class WelcomeActivity extends Activity {

	private ProgressDialog pDialog;
	Button buttonGet, buttonSet, finishBordur;
	RelativeLayout layoutButton, AcceptDecline;

	String order = null;
	String pickup, pay_type, total_pay;
	String destination;
	String title;
	String comments;
	String username, driver_id, car_id, status;
	String id = "id";

	TextView textView2;

	JSONArray orders = null;

	JSONObject json;

	JSONParser jsonParser = new JSONParser();

	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String ACTION_GET = "getOrder";
	private static final String ACTION_SET = "setOrderStatus";
	private static final String TAG_LOCATAION_FROM = "location_from";
	private static final String TAG_LOCATAION_TO = "location_to";
	private static final String TAG_PICKUP_DATATIME = "pickup_datetime";
	private static final String TAG_COMMENTS = "comments";
	private static final String TAG_TYPE = "payment_type";
	private static final String TAG_PAY = "total";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(WelcomeActivity.this);
		username = sp.getString("username", "anon");
		car_id = sp.getString("car_id", "123");
		driver_id = sp.getString("id", "anon");
		buttonGet = (Button) findViewById(R.id.getOrder);
		buttonSet = (Button) findViewById(R.id.acceptOrder);
		finishBordur = (Button) findViewById(R.id.finishBordur);

		layoutButton = (RelativeLayout) findViewById(R.id.layoutButtons);
		AcceptDecline = (RelativeLayout) findViewById(R.id.AcceptDecline);

		textView2 = (TextView) findViewById(R.id.textView2);
		
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    }
	}
	




  private void buildAlertMessageNoGps() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("GPS на вашем телефоне отключен, желаете его включить?")
           .setCancelable(false)
				.setPositiveButton("Да",
						new DialogInterface.OnClickListener() {
               public void onClick(final DialogInterface dialog, final int id) {
                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
               }
           })
           .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
               public void onClick(final DialogInterface dialog, final int id) {
                    dialog.cancel();
               }
           });
    final AlertDialog alert = builder.create();
    alert.show();
}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (extras != null) {
			if (layoutButton.getVisibility() != View.VISIBLE
					&& AcceptDecline.getVisibility() != View.GONE) {
				AcceptDecline.setVisibility(View.VISIBLE);
			} else {
				layoutButton.setVisibility(View.INVISIBLE);
				AcceptDecline.setVisibility(View.VISIBLE);
			}
			if (extras.containsKey("id")) {
				order = extras.getString("id");
				new GetOrders().execute();
			} else {
				MyCustomReceiver customRec = new MyCustomReceiver();
				customRec.onReceive(this, getIntent());
			}
		}
		/*
		 * if (getIntent().getExtras() != null) { MyCustomReceiver customRec =
		 * new MyCustomReceiver(); customRec.onReceive(this, getIntent()); }
		 */
		ParseAnalytics.trackAppOpened(getIntent());

		startService(new Intent(this, GPSService.class));

	}

	public void backButton(View v) {
		onBackPressed();
		finish();
	}

	class AddOrder extends AsyncTask<String, String, String> {

		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(WelcomeActivity.this);
			pDialog.setMessage("Прием заказа!");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_SET));
			params.add(new BasicNameValuePair("order", order));
			params.add(new BasicNameValuePair("driver_id", driver_id));
			params.add(new BasicNameValuePair("status", "1"));

			Log.d("request!", "starting");

			// Posting user data to script
			JSONArray jsonArr = jsonParser.makeArrayRequest(LOGIN_URL, "POST",
					params);

			Log.d("ORDER", jsonArr.toString());
			// json success element

			return null;

		}

		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			//сохраняем ID заказа
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(WelcomeActivity.this);
			Editor edit = sp.edit();
			edit.putString("order", order);
			edit.commit();
			Intent in = new Intent(WelcomeActivity.this, OrderType.class);
			startActivity(in);
			finish();

		}

	}

	public class MyCustomReceiver extends BroadcastReceiver {
		private static final String TAG = "MyCustomReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			try {
				json = new JSONObject(intent.getExtras().getString(
						"com.parse.Data"));

				String notification = json.getString("alert");
				Log.d("Notification: ", notification);
				String[] parts = new String[2];
				parts = notification.split("№");
				order = parts[1];
				new GetOrders().execute();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG, "JSONException: " + e.getMessage());
			}

		}
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startService(new Intent(this, GPSService.class));
	}

	// получить заказ
	private class GetOrders extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(WelcomeActivity.this);
			pDialog.setMessage("Попытка соединения");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			// String order = orderEdit.getText().toString();
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_GET));
			params.add(new BasicNameValuePair("order", order));
			// params.add(new BasicNameValuePair("password", pwd));

			Log.d("request!", "starting");
			JSONArray json = jsonParser.makeArrayRequest(LOGIN_URL, "POST",
					params);

			if (json.length() != 0) {
				Log.d("json", json.toString());
				try {
					for (int i = 0; i < json.length(); i++) {
						JSONObject c = json.getJSONObject(i);
						pickup = c.getString(TAG_LOCATAION_FROM);
						destination = c.getString(TAG_LOCATAION_TO);
						title = c.getString(TAG_PICKUP_DATATIME);
						comments = c.getString(TAG_COMMENTS);
						pay_type = c.getString(TAG_TYPE);
						if ("3".equals(pay_type)) {
							total_pay = c.getString(TAG_PAY);
							Log.d("TOTAL", total_pay);
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(WelcomeActivity.this);
			Editor edit = sp.edit();
			edit.putString("payment_type", pay_type);
			edit.putString("total_pay", total_pay);
			edit.commit();

			pDialog.dismiss();
			if ("3".equals(pay_type)) {
				textView2.setText(pickup + "\n" + destination + "\n" + title
						+ "\n" + "Оплата: " + total_pay + " ТГ");
			} else {
				textView2.setText(pickup + "\n" + destination + "\n" + title
						+ "\n");
			}
		}

	}

	public void freeOrders(View v) {
		Intent in = new Intent(WelcomeActivity.this, GetOrder.class);
		startActivity(in);
	}

	public void exitAction(View v) {
		Intent in = new Intent(WelcomeActivity.this, Login_Activity.class);
		startActivity(in);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(WelcomeActivity.this);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove("login");
		editor.commit();
	}

	public void fromBordur(View v) {
		layoutButton.setVisibility(View.GONE);
		finishBordur.setVisibility(View.VISIBLE);
		textView2.setText("Заказ с бордюра!");
		status = "3";
		new setCarStatus().execute();
	}
	
	private class setCarStatus extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(WelcomeActivity.this);
			pDialog.setMessage("Отправка данных!");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			// String order = orderEdit.getText().toString();
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", "setCarStatus"));
			params.add(new BasicNameValuePair("car_id", car_id));
			params.add(new BasicNameValuePair("status", status));

			Log.d("request!", "starting");
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
					params);

			Log.d("JSON", json.toString());
			
			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {

			pDialog.dismiss();
		}

	}

	public void StopOrder(View v) {
		finishBordur.setVisibility(View.GONE);
		layoutButton.setVisibility(View.VISIBLE);
		textView2.setText(R.string.noOrdersText);
		status = "0";
		new setCarStatus().execute();
	}

	public void acceptOrder(View v) {
		new AddOrder().execute();
		
	}

	public void declineOrder(View v) {
		order = null;
		AcceptDecline.setVisibility(View.GONE);
		layoutButton.setVisibility(View.VISIBLE);
		textView2.setText(R.string.noOrder);

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(new Intent(this, GPSService.class));
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
}
