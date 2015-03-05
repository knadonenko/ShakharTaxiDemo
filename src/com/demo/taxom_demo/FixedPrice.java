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
import android.widget.TextView;

public class FixedPrice extends Activity {
	
	String total, driver_id, order_id;
	String order_status = "2";
	TextView price;
	Button finishButton;
	Boolean finished = false;
	
	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String ACTION_SET = "setOrderStatus";
	
	JSONParser jsonParser = new JSONParser();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fixed_price);
		//total = getIntent().getExtras().getString("total_pay");
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FixedPrice.this);
		total = sp.getString("total_pay", "Бесценная поездка!");
		
		price = (TextView) findViewById(R.id.price);
		price.setText("Оплата: " + total + "ТГ");
		finishButton = (Button) findViewById(R.id.buttonFinish);
		
	}
	
	public void backButton(View v) {
		
		if (finished == false) {
			order_status = "0";
			new setOrderStatus().execute();
			Intent in = new Intent(FixedPrice.this, WelcomeActivity.class);
			startActivity(in);
			finish();
		} else {
			finish();
		}
		
	}
	
	public void ChangeOrderStatus(View v) {
		finished = true;
		new setOrderStatus().execute();
		finishButton.setVisibility(View.GONE);
	}
	
	class setOrderStatus extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			// Building Parameters

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(FixedPrice.this);
			driver_id = sp.getString("id", "anon");
			order_id = sp.getString("order", "0");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_SET));
			params.add(new BasicNameValuePair("order", order_id));
			params.add(new BasicNameValuePair("driver_id", driver_id));
			params.add(new BasicNameValuePair("status", order_status));

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
}
