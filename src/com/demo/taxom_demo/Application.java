package com.demo.taxom_demo;

import com.parse.Parse;
import com.parse.PushService;

public class Application extends android.app.Application {

  public Application() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

	// Initialize the Parse SDK.
    Parse.initialize(this, "3wMsuECKQYhdOoHOwZM6iZbM7tZWy24X1gAbiadf", "Oe2w3kWdwFgi7vtBNzo9ojQNP6F6mb85MfBfohQw"); 

	// Specify an Activity to handle all pushes by default.
	PushService.setDefaultPushCallback(this, WelcomeActivity.class);
  }
}