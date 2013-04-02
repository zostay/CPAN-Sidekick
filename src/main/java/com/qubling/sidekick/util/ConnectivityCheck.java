package com.qubling.sidekick.util;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.qubling.sidekick.R;

import android.app.Activity;
import android.widget.Toast;

public class ConnectivityCheck extends Thread {
    public Activity activity;
    
    public ConnectivityCheck(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        HttpClient testConnectivityClient = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://api.metacpan.org/");
        try {
            testConnectivityClient.execute(request);
        }
        catch (IOException e) {
            activity.runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    Toast.makeText(activity, R.string.cannot_connect_to_metacpan, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
