package com.example.myfirstapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    Messenger mService = null;
    boolean mBound;

    /**
     * ResponseHandler -- handles replies from the service
     */
    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            
            for (String key : data.keySet()) {
                Object obj = data.get(key);
                Log.d("RECEIVED_ITEMS", String.format("%s %s (%s)", key,  
                    obj.toString(), obj.getClass().getName()));
            }
            
            return;
        }
    }
    final Messenger mMessenger = new Messenger(new ResponseHandler());

    
 	public void sendMessage(View view) {
        if (!mBound) return;

		EditText sText = (EditText) findViewById(R.id.edit_seed);
		EditText rText = (EditText) findViewById(R.id.edit_ratio);

        Bundle fuzz = new Bundle();
        fuzz.putString(EXTRA_MESSAGE, 
        		sText.getText().toString());
        
        Bundle data = new Bundle();
        data.putBundle("mut_data", fuzz);
        data.putFloat("mut_ratio",
        		Float.parseFloat(rText.getText().toString()));
        data.putString("ipc_target_package",
        		"com.example.myfirstapp");
        data.putString("ipc_target_component",
        		"com.example.myfirstapp.DisplayMessageActivity");
        
        Message msg = Message.obtain(null, 1, 0, 3);
        msg.replyTo = mMessenger;
        msg.setData(data);

 		Log.i("MyApp", "Sending message");
 		
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
 		
    	return;
	}
    
    /**
     * ServiceConnection -- handles connection stuffs
     */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,IBinder service) {
            Log.i("MyApp", "Connection established");
            
            mService = new Messenger(service);
            mBound = true;
            
            return;
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i("MyApp", "Connection lost");
            
            mService = null;
            mBound = false;
            
            return;
        }
    };
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the service
 		Log.i("MyApp", "Binding service");
    	Intent intent = new Intent();
    	intent.setClassName("com.example.myfirstservice",
    			"com.example.myfirstservice.MyService");
    	bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
        super.onDestroy();
        
        // Unbind from the service
 		Log.i("MyApp", "Uninding service");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}
	
}
