package com.demo.taxom_demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ConfigActivity extends Activity {
	
	EditText editConfig;
	String car_id;
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		editConfig = (EditText) findViewById(R.id.editConfig);
		
		sp = PreferenceManager.getDefaultSharedPreferences(ConfigActivity.this);
		String SavedCar_id = sp.getString("car_id", "Введите номер автомобиля");
		editConfig.setText(SavedCar_id);
	}
	
	public void saveConfig (View v) {
		car_id = editConfig.getText().toString();
    	Editor edit = sp.edit();
    	edit.putString("car_id", car_id);
    	edit.commit();
    	String get_car = sp.getString("car_id", "anon");
    	Log.d("CAR", get_car);
    	Intent in = new Intent(ConfigActivity.this, Login_Activity.class);
    	startActivity(in);
    	finish();
	}
}
