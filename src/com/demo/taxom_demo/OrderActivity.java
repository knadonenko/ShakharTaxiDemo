package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class OrderActivity extends Activity {

	final String LOG_TAG = "myLogs";
	int cnt = 0;
	ProgressDialog pDialog;

	TextView orderText, orderType, measurement, measurmentType, moneySum;
	Button buttonFinish;
	Boolean order;
	Boolean finished = false;
	String time, distance;
	String order_status = "2";
	Timer t = new Timer();

	private Handler customHandler = new Handler();

	private long startTime;

	long timeInSeconds;
	long timeSwapBuff;
	long updatedTime;
	long overallTime;
	int secs;
	int mins;

	// JSONParams
	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String TAG_ACTION = "setOrderData";
	private static final String ACTION_SET = "setOrderStatus";
	String driver_id;
	String order_id;
	String car_id;
	String type;
	String total;

	// For keeping track location
	GPSTracker gps;
	private Handler kmHandler = new Handler();
	double begin_lat;
	double begin_long;
	double end_lat;
	double end_long;
	double updated_distance;
	double final_distance;
	// Counter for
	int coefCount = 0;
	int coef = 1;

	JSONParser jsonParser = new JSONParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);
		// Не дает экрану заснуть
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		orderType = (TextView) findViewById(R.id.tarifType); // тип
		// тарификации
		measurement = (TextView) findViewById(R.id.measurement); // измерение в
																	// км или
																	// минутах
		measurmentType = (TextView) findViewById(R.id.measureType); // тип
																	// измерения
		moneySum = (TextView) findViewById(R.id.moneySum);
		// getOrderType = (Button) findViewById(R.id.getOrder);
		buttonFinish = (Button) findViewById(R.id.buttonFinish);

		// включаем определенный лэйаут для заказа
		if (getIntent().getExtras() != null) {
			order = getIntent().getExtras().getBoolean("orderType");
			OrderSet(order);
			Log.d("order", order.toString());
		}


	}

	public void backButton(View v) {

		//onBackPressed();
		if (finished == false) {
			Intent in = new Intent(OrderActivity.this, WelcomeActivity.class);
			startActivity(in);
			order_status = "0";
			new setOrderStatus().execute();
			finish();
			stopService(new Intent(this, CheckOrder.class));
		} else {
			finish();
		}
		

	}

	public void OpenOrderType(View v) {
		Intent i = new Intent(this, OrderType.class);
		startActivity(i);
	}

	// установка для заказа
	private void OrderSet(Boolean CheckOrder) {

		moneySum.setText("0");

		if (!CheckOrder) {
			orderType.setText(R.string.orderTypeKM);
			measurmentType.setText(R.string.measureKM);
			updateGeo.run();
			type = "2";
		} else {
			orderType.setText(R.string.orderTypeTime);
			measurmentType.setText(R.string.measureTime);
			startTime = SystemClock.uptimeMillis();
			customHandler.postDelayed(updateTimerThread, 0);
			type = "1";
		}

	}
	
	//считается заказ
	public void CountOrder(View v) {
		finished = true;
		buttonFinish.setVisibility(View.GONE);
		new sendOrderData().execute();
		Log.d("TYPE", type);

		if (!order) {
			kmHandler.removeCallbacks(updateGeo);

			if (final_distance % coef > 0.5) {
				coefCount++;
			}
			double sumOfOrder = coefCount * 100.0;
			measurement.setText("" + String.format("%.1f", final_distance));
			moneySum.setText("" + String.format("%.0f", sumOfOrder));
			total = String.format("%.0f", sumOfOrder);
		} else {
			timeSwapBuff += timeInSeconds;
			customHandler.removeCallbacks(updateTimerThread);
			overallTime = (int) ((timeSwapBuff / (1000 * 60)) % 60);
			Log.d("time", Long.toString(overallTime));
			if (secs >= 30) {
				overallTime += 1;
			}
			int moneyToPay = (int) (overallTime * 22);
			moneySum.setText("" + moneyToPay);
			total = String.valueOf(moneyToPay);
		}
	}

	public void stopOrderKM(View v) {
		kmHandler.removeCallbacks(updateGeo);

		if (final_distance % coef > 0.5) {
			coefCount++;
		}
		double sumOfOrder = coefCount * 150.0;
		measurement.setText("" + String.format("%.1f", final_distance));
		moneySum.setText("" + String.format("%.0f", sumOfOrder));
	}

	private Runnable updateGeo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			gps = new GPSTracker(OrderActivity.this);

			if (gps.canGetLocation()) {

				begin_lat = gps.getLatitude();
				begin_long = gps.getLongitude();
				Log.d("Lat", Double.toString(begin_lat));
				Log.d("Long", Double.toString(begin_long));

				// Toast.makeText(getApplicationContext(),
				// "Lat: \n" + begin_lat + "Long: \n" + begin_long,
				// Toast.LENGTH_SHORT).show();
				float[] results = new float[1];
				if (begin_lat > 1 && begin_long > 1 && end_lat > 1
						&& end_long > 1) {
					Location.distanceBetween(end_lat, end_long, begin_lat,
							begin_long, results);
					updated_distance = results[0];
				}
				Log.d("DISTANCE", Arrays.toString(results));

				// обновляем конечные координаты
				end_lat = begin_lat;
				end_long = begin_long;

				// конечная дистанция
				final_distance += (updated_distance) / 1000;

				// Коэффициенты для подсчета стоимости
				if (final_distance >= coef) {
					coefCount++;
					coef++;
				}

				double sumOfOrder = coefCount * 150.0;
				measurement.setText("" + String.format("%.1f", final_distance));
				moneySum.setText("" + String.format("%.0f", sumOfOrder));

			}
			kmHandler.postDelayed(updateGeo, 10000);
		}
	};

	class sendOrderData extends AsyncTask<String, String, String> {

		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(OrderActivity.this);
			pDialog.setMessage("Отправка на сервер!");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Building Parameters

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(OrderActivity.this);
			driver_id = sp.getString("id", "anon");
			order_id = sp.getString("order", "0");
			car_id = sp.getString("car_id", "0");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", TAG_ACTION));
			params.add(new BasicNameValuePair("order", order_id));
			params.add(new BasicNameValuePair("car_id", car_id));
			params.add(new BasicNameValuePair("driver_id", driver_id));
			params.add(new BasicNameValuePair("type", type));
			params.add(new BasicNameValuePair("time", Long
					.toString(overallTime)));
			params.add(new BasicNameValuePair("kms", String.format("%.1f",
					final_distance)));
			params.add(new BasicNameValuePair("total", total));

			Log.d("request!", "starting");

			// Posting user data to script
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
					params);

			// full json response
			if (json.length() != 0) {
				Log.d("SEND", json.toString());
			}
			// json success element

			return null;

		}

		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			new setOrderStatus().execute();
		}

	}

	// обновляем таймер
	private Runnable updateTimerThread = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			timeInSeconds = SystemClock.uptimeMillis() - startTime;

			updatedTime = timeSwapBuff + timeInSeconds;

			secs = (int) (updatedTime / 1000);
			mins = secs / 60;
			secs = secs % 60;
			measurement.setText("" + mins + ":" + String.format("%02d", secs));
			overallTime = (int) ((timeInSeconds / (1000 * 60)) % 60);
			int moneyToPay = (int) (overallTime * 22);
			moneySum.setText("" + moneyToPay);
			customHandler.postDelayed(this, 0);
		}
	};

	public void StopOrder(View v) {
		orderType.setText(R.string.noOrdersText);
		timeSwapBuff += timeInSeconds;
		customHandler.removeCallbacks(updateTimerThread);
		overallTime = (int) ((timeSwapBuff / (1000 * 60)) % 60);
		Log.d("time", Long.toString(overallTime));
		if (secs >= 30) {
			overallTime += 1;
		}
		int moneyToPay = (int) (overallTime * 22);
		moneySum.setText("" + moneyToPay);

	}

	class setOrderStatus extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Building Parameters

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(OrderActivity.this);
			driver_id = sp.getString("id", "anon");
			order_id = sp.getString("order", "0");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_SET));
			params.add(new BasicNameValuePair("order", order_id));
			params.add(new BasicNameValuePair("driver_id", driver_id));
			params.add(new BasicNameValuePair("status", "2"));

			Log.d("request!", "starting");

			// Posting user data to script
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
					params);

			// full json response
			Log.d("Login attempt", json.toString());

			// json success element

			return null;

		}

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("timeInSeconds", timeInSeconds);
		outState.putLong("starting", startTime);
		outState.putLong("timeSwap", timeSwapBuff);
		outState.putLong("update", updatedTime);
		outState.putLong("overallTime", overallTime);
		outState.putInt("sec", secs);
		outState.putInt("mins", mins);
		outState.putDouble("begin_lat", begin_lat);
		outState.putDouble("begin_long", begin_long);
		outState.putDouble("end_lat", end_lat);
		outState.putDouble("end_long", end_long);
		outState.putDouble("updated_distance", updated_distance);
		outState.putDouble("final_distance", final_distance);
		outState.putInt("coefCount", coefCount);
		outState.putInt("coef", coef);
		Log.d(LOG_TAG, "onSaveInstanceState");
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		timeInSeconds = savedInstanceState.getLong("timeInSeconds");
		startTime = savedInstanceState.getLong("starting");
		timeSwapBuff = savedInstanceState.getLong("timeSwapBuff");
		updatedTime = savedInstanceState.getLong("update");
		overallTime = savedInstanceState.getLong("overallTime");
		secs = savedInstanceState.getInt("sec");
		mins = savedInstanceState.getInt("mins");

		begin_lat = savedInstanceState.getDouble("begin_lat");
		begin_long = savedInstanceState.getDouble("begin_long");
		end_lat = savedInstanceState.getDouble("end_lat");
		end_long = savedInstanceState.getDouble("end_long");
		updated_distance = savedInstanceState.getDouble("updated_distance");
		final_distance = savedInstanceState.getDouble("final_distance");
		// Counter for
		coefCount = savedInstanceState.getInt("coefCount");
		coef = savedInstanceState.getInt("coef");
		Log.d(LOG_TAG, String.valueOf(timeInSeconds));
	}
}
