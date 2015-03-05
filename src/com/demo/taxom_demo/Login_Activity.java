package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login_Activity extends Activity implements OnClickListener {

	EditText username, password;
	Button loginButton, registerButton;
	AlertDialog alertDialog;
	SharedPreferences userPrefs;

	String login;
	private static String get_username, getCarId;

	ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();
	JSONObject json;
	
	// flag for Internet connection status
    Boolean isInternetPresent = false;
     
    // Connection detector class
    ConnectionCheck cd;

	// JSON messages
	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String ACTION_ID = "login";
	private static final String TAG_ID = "id";
	private static final String TAG_STATUS = "status";
	String status = null;
	String id = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		cd = new ConnectionCheck(getApplicationContext());
		isInternetPresent = cd.isConnectingToInternet();

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		// setup buttons
		loginButton = (Button) findViewById(R.id.login);

		// register listeners
		loginButton.setOnClickListener(this);
		userPrefs = PreferenceManager
				.getDefaultSharedPreferences(Login_Activity.this);
		get_username = userPrefs.getString("login", "anon");
		getCarId = userPrefs.getString("car_id", "anon");
		
		if(!isInternetPresent) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					Login_Activity.this);
			alertDialogBuilder.setTitle("Подключение к интернету отсутствует");
			alertDialogBuilder
					.setMessage("Требуется подключение к интернету")
					.setCancelable(false)
					.setPositiveButton("Ок",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int id) {
									// TODO Auto-generated method stub
									startActivity(new Intent(
									        Settings.ACTION_AIRPLANE_MODE_SETTINGS));
								}
							});

			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		if (get_username != "anon" && isInternetPresent) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					Login_Activity.this);
			alertDialogBuilder.setTitle("Желаете залогиниться под");
			alertDialogBuilder
					.setMessage("Желаете залогиниться под: " + get_username)
					.setCancelable(false)
					.setPositiveButton("Да",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// TODO Auto-generated method stub
									Intent i = new Intent(Login_Activity.this,
											WelcomeActivity.class);
									startActivity(i);
								}
							})
					.setNegativeButton("Нет",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							});

			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			
			
			}

		

	}

	/*
	 * @Override public void onStart() { super.onStart();
	 * 
	 * // Display the current values for this user, such as their age and
	 * gender. displayUserProfile(); refreshUserProfile(); }
	 */

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.login:
			if (username.getText().length() == 0
					&& password.getText().length() == 0) {
				Toast.makeText(Login_Activity.this,
						"Введите имя пользователя и пароль", Toast.LENGTH_LONG)
						.show();

			} else {
				if (getCarId == "anon") {
					Toast.makeText(Login_Activity.this,
							"Введите номер автомобиля!", Toast.LENGTH_LONG)
							.show();
					Intent in = new Intent(Login_Activity.this, ConfigActivity.class);
					startActivity(in);
				} else
					new AttemptLogin().execute();
				break;
			}
		default:
			break;
		}

	}

	private class AttemptLogin extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Login_Activity.this);
			pDialog.setMessage("Попытка соединения");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			login = username.getText().toString();
			String pwd = password.getText().toString();
			// try {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_ID));
			params.add(new BasicNameValuePair("login", login));
			params.add(new BasicNameValuePair("pwd", pwd));

			Log.d("request!", "starting");
			// sending HTTP Post to get right JSON
			json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);

			// check your log for json response
			Log.d("Login attempt", json.toString());

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {

			pDialog.dismiss();
			if (json.has("name")) {

				try {
					id = json.getString(TAG_ID);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(Login_Activity.this);
				Editor edit = sp.edit();
				edit.putString("id", id);
				edit.putString("login", login);
				edit.commit();

				Intent i = new Intent(Login_Activity.this,
						WelcomeActivity.class);
				finish();
				startActivity(i);
			} else if (json.has(TAG_STATUS)) {
				Toast.makeText(Login_Activity.this, "Неправильно введено имя пользователя или пароль", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void openConfig(View v) {
		Intent ins = new Intent(Login_Activity.this, ConfigActivity.class);
		startActivity(ins);
	}

	protected void onStop() {
		super.onStop();
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}

}
