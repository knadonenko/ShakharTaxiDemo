package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OrderType extends Activity {

	String order_type, order, car_id, arriving_at;

	Button distance, time, inPlace;
	TextView orderNumber;
	RelativeLayout layoutButton;
	
	
	JSONParser jsonParser = new JSONParser();
	
	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	//private static final String setArrivingTime = "setArrivingTime";
	private static final String TAG_READY = "setReadyToPickup";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_type);

		distance = (Button) findViewById(R.id.buttonTime);
		time = (Button) findViewById(R.id.buttonDistance);
		inPlace = (Button) findViewById(R.id.inPlace);
		
		layoutButton = (RelativeLayout) findViewById(R.id.layoutTime);
		
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(OrderType.this);
		order_type = sp.getString("payment_type", "No payment");
		order = sp.getString("order", "Нет номера заказа!");
		car_id = sp.getString("car_id", "Нет номера автомобиля");
		System.out.println(order);
		Log.d("ORDER_NUM", order);
		Log.d("car_id", car_id);

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startService(new Intent(this, CheckOrder.class));
	}
	
	public void backButton(View v) {
		onBackPressed();
		finish();
	}
	
	public void tenMinutes(View v) {
		layoutButton.setVisibility(View.GONE);
		inPlace.setVisibility(View.VISIBLE);
		arriving_at = "10";
		new Setarriving_atTime().execute();
		Log.d("ARRIVING", arriving_at);
	}
	
	public void fifteenMinutes(View v) {
		layoutButton.setVisibility(View.GONE);
		inPlace.setVisibility(View.VISIBLE);
		arriving_at = "15";
		Log.d("ARRIVING AT", arriving_at);
		new Setarriving_atTime().execute();
	}
	
	public void twentyMinutes(View v) {
		layoutButton.setVisibility(View.GONE);
		inPlace.setVisibility(View.VISIBLE);
		arriving_at = "20";
		Log.d("ARRIVING", arriving_at);
		new Setarriving_atTime().execute();
	}
	
	public void inPlace(View v) {
		// TODO Auto-generated method stub
		//Intent in = new Intent(OrderType.this, OrderActivity.class);
		//startActivity(in);
		new setReadyToPickup().execute();
		if ("3".equals(order_type)) {
			Intent in = new Intent(OrderType.this, FixedPrice.class);
			startActivity(in);
			finish();
		} else if ("1".equals(order_type)) {
			SetTimer();
			finish();
		} else if ("2".equals(order_type)) {
			SetDistancer();
			finish();
		}
	}

	public void SetTimer() {
		Intent time = new Intent(this, OrderActivity.class);
		time.putExtra("orderType", true);
		startActivity(time);
		//new AddOrder().execute();
	}

	public void SetDistancer() {
		Intent distancer = new Intent(this, OrderActivity.class);
		distancer.putExtra("orderType", false);
		startActivity(distancer);
	}

	//arrivintime
	class Setarriving_atTime extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", "setArrivingTime"));
			params.add(new BasicNameValuePair("order", order));
			params.add(new BasicNameValuePair("car_id", car_id));
			params.add(new BasicNameValuePair("arriving_at", arriving_at));

			Log.d("request!", "starting");
			Log.d("LIST", params.toString());

			// Posting user data to script
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
					params);
			
			Log.d("json", json.toString());
			
			return null;
		}
		
	}
	
	class setReadyToPickup extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", TAG_READY));
			params.add(new BasicNameValuePair("order", order));
			params.add(new BasicNameValuePair("car_id", car_id));

			Log.d("request!", "starting");

			// Posting user data to script
			JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
					params);
			
			Log.d("READYTOPICKUP", json.toString());
			return null;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(new Intent(this, CheckOrder.class));
	}



}
