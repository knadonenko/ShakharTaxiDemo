package com.demo.taxom_demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class GetOrder extends Activity {

	//private ProgressDialog pDialog;
	JSONArray json;
	int index = 0;

	String pickup, destination, id;

	ListView list;

	// JSON parameters
	private static final String LOGIN_URL = "http://taxishahar.kz/taxi/api/api.php";
	private static final String api_key = "a2b6e60908723820e9b3560f481e394add01a32ac539d7f7844e57fddde89d76";
	private static final String ACTION_ID = "getOrders";
	private static final String TAG_LOCATAION_FROM = "location_from";
	private static final String TAG_LOCATAION_TO = "location_to";
	private static final String TAG_ID = "id";
	private static final String TAG_RESULT = "result";
	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> oslist = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_order);

		list = (ListView) findViewById(R.id.list);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		new getOrders().execute();

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String id_n = ((TextView) view.findViewById(R.id.id)).getText()
						.toString();

				Intent in = new Intent(getApplicationContext(),
						WelcomeActivity.class);
				in.putExtra(TAG_ID, id_n);
				startActivity(in);
				finish();
			}
		});
	}

	public void backButton(View v) {
		onBackPressed();
	}

	class getOrders extends AsyncTask<String, String, String> {

		// @Override
		/*
		 * protected void onPreExecute() { // TODO Auto-generated method stub
		 * super.onPreExecute(); pDialog = new ProgressDialog(GetOrder.this);
		 * pDialog.setMessage("Обновляем список заказов");
		 * pDialog.setIndeterminate(false); pDialog.setCancelable(true);
		 * pDialog.show(); }
		 */

		@Override
		protected String doInBackground(String... arg0) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("api_key", api_key));
			params.add(new BasicNameValuePair("act", ACTION_ID));

			Log.d("request!", "starting");

			// отправляем JSON
			json = jsonParser.makeArrayRequest(LOGIN_URL, "POST", params);

			return null;
		}

		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted pDialog.dismiss();

			try {
				for (int i = 0; i < json.length(); i++) {
					JSONObject c = json.getJSONObject(i);
					if (c.has(TAG_ID)) {
						pickup = c.getString(TAG_LOCATAION_FROM);
						destination = c.getString(TAG_LOCATAION_TO);
						id = c.getString(TAG_ID);

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(TAG_LOCATAION_FROM, pickup);
						map.put(TAG_LOCATAION_TO, destination);
						map.put(TAG_ID, id);

						oslist.add(map);

						ListAdapter adapter = new SimpleAdapter(GetOrder.this,
								oslist, R.layout.list_v, new String[] {
										TAG_LOCATAION_FROM, TAG_LOCATAION_TO,
										TAG_ID }, new int[] { R.id.from,
										R.id.to, R.id.id });
						list.setAdapter(adapter);
					} else if (c.has(TAG_RESULT)) {
						Toast.makeText(GetOrder.this, "Свободных заказов нет!", Toast.LENGTH_LONG).show();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}
}
