package com.example.bluetoothtest;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

// Bluetooth implementation based on http://examples.javacodegeeks.com/android/core/bluetooth/bluetoothadapter/android-bluetooth-example/
public class MainActivity extends Activity
{
	// HANDLER MESSAGE TYPES
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	// REQUEST CODES
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    
    // HANDLER KEY NAMES
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    
	// VIEW AND LAYOUT
    private Button buttonFetch;
	private Button buttonSend;
	
	// COMMUNICATION VARIABLES
	private BluetoothConnection connection;
	private String mConnectedDeviceName = null;
	private StringBuffer mOutStringBuffer;
	
	private ArrayList<String> pairedDevices = new ArrayList<String>();
	private ArrayList<String> otherDevices = new ArrayList<String>();
	//private ArrayList<String> messages = new ArrayList<String>();
	
	private ListView pairedDeviceList;
	private ListView otherDeviceList;
	private ArrayAdapter<String> pairedDevAdapter;	
	private ArrayAdapter<String> otherDevAdapter;
	
	private BluetoothAdapter adapter;
	private Set<BluetoothDevice> devices;
	private SharedPreferences preferences;
	
	final Context context = this;
	private BluetoothDevice dev;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		adapter = BluetoothAdapter.getDefaultAdapter();
		
		if (adapter == null)
		{
			// alert the user of incompatibility before closing (finishing) the app
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Bluetooth is not supported on your device!")
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener()
			       {
			           public void onClick(DialogInterface dialog, int id)
			           {
			        	   finish();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}
		else
		{			
			//Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show(); //for displaying popup message
			setContentView(R.layout.activity_main);
			
			pairedDeviceList = (ListView) findViewById(R.id.pairedDeviceList);
			pairedDevAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevices);
			pairedDeviceList.setAdapter(pairedDevAdapter);
			
			otherDeviceList = (ListView) findViewById(R.id.otherDeviceList);
			otherDevAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, otherDevices);
			otherDeviceList.setAdapter(otherDevAdapter);
			
			pairedDeviceList.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
				{
					setupConnection(v, position, pairedDevAdapter);
				}
			});
			
			otherDeviceList.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
				{
					setupConnection(v, position, otherDevAdapter);
				}
			});
			
			buttonFetch = (Button) findViewById(R.id.buttonFetch);
			buttonFetch.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
					startActivity(discoverableIntent);
					
					showPairedDevices(v);
					searchDevices(v);
				}
			});
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		// If BT is not on, request that it be enabled.
		if (!adapter.isEnabled())
		{
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		connection = new BluetoothConnection(this, mHandler);
	}
 
	@Override
	public synchronized void onResume()
	{
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (connection != null)
		{
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (connection.getState() == BluetoothConnection.STATE_NONE)
			{
				connection.start();
			}
		}
	}
	
	public void setDevice(BluetoothDevice device)
	{
		dev = device;
	}
	
	public BluetoothDevice getDevice()
	{
		return dev;
	}
	
	protected void setupConnection(View v, int position, ArrayAdapter<String> arrayAdapter)
	{
		// obtain MAC address of clicked device
		String deviceDescriptor = arrayAdapter.getItem(position);
		int length = deviceDescriptor.length();
		String mac = deviceDescriptor.substring(length-17, length);
		
		// reconstruct device using MAC address
		BluetoothDevice device = adapter.getRemoteDevice(mac);
		setDevice(device);
		
		String name = device.getName();
		if (name == null)
		{
			Toast.makeText(getApplicationContext(), "This device is nowhere to be found!", Toast.LENGTH_SHORT).show();
		}
		else
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(context);
			adb.setMessage("Connect to " + name + " ?")
			.setCancelable(false)
			.setPositiveButton("Accept", new DialogInterface.OnClickListener()
			{
		    	public void onClick(DialogInterface dialog, int id)
		    	{
		    		connection.connect(getDevice(), false);
	    		}
		    })
	        .setNegativeButton("Decline", new DialogInterface.OnClickListener()
	        {
	        	public void onClick(DialogInterface dialog, int id)
	        	{
	        		dialog.cancel();
	        	}
	        });
	        AlertDialog a = adb.create();
	        a.show();
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (connection != null)
			connection.stop();
	}
	
	//not sure what to do with these functions; don't seem entirely necessary
    private final void setStatus(int resId)
    {
        //final ActionBar actionBar = getActionBar();
        //actionBar.setSubtitle(resId);
        
        //if (BluetoothMessage.actionBar != null)
        //	BluetoothMessage.actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle)
    {
        //final ActionBar actionBar = getActionBar();
        //actionBar.setSubtitle(subTitle);
        
        //if (BluetoothMessage.actionBar != null)
        //	BluetoothMessage.actionBar.setSubtitle(subTitle);
    }
	
	private final Handler mHandler = new Handler()
	{
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1)
                {
                case BluetoothConnection.STATE_CONNECTED:
                    //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    break;
                case BluetoothConnection.STATE_CONNECTING:
                    //setStatus(R.string.title_connecting);
                    break;
                case BluetoothConnection.STATE_LISTEN:
                case BluetoothConnection.STATE_NONE:
                case BluetoothConnection.STATE_BUSY:
                    //setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                //String writeMessage = new String(writeBuf);
                Intent intent_write = new Intent(getApplicationContext(), BluetoothMessage.class);
                intent_write.putExtra("source", "Me");
                intent_write.putExtra("buf", writeBuf);
        		startActivity(intent_write);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, msg.arg1);
                Intent intent_read = new Intent(getApplicationContext(), BluetoothMessage.class);
                intent_read.putExtra("source", mConnectedDeviceName);
                intent_read.putExtra("buf", readBuf);
                intent_read.putExtra("size", msg.arg1);
        		startActivity(intent_read);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                BluetoothMessage.myConnection = connection;
                BluetoothMessage.myAdapter = adapter;
        		Intent intent_new = new Intent(getApplicationContext(), BluetoothMessage.class);
        		startActivity(intent_new);
        		break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
	protected void toggleBluetooth(View v)
	{
		if(!adapter.isEnabled())
		{
			// if adapter is not enabled, start Bluetooth
			Intent intent = new Intent(adapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		}
		else
		{
			// otherwise, Bluetooth is enabled, so we disable it
			adapter.disable();
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode==REQUEST_ENABLE_BT)
		{
			//if we want to do something
		}
	}
	
	protected void showPairedDevices(View v)
	{
		devices = adapter.getBondedDevices();
		pairedDevAdapter.clear();
		
		for (BluetoothDevice d : devices)
		{
			pairedDevAdapter.add(d.getName() + "\n" + d.getAddress());
		}
			
	}
	
	final BroadcastReceiver receiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			otherDevAdapter.clear();
			
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				otherDevAdapter.add(d.getName() + "\n" + d.getAddress());
				otherDevAdapter.notifyDataSetChanged();
			}	
		}
	};
	
	protected void searchDevices(View v)
	{
		if(adapter.isDiscovering())
		{
			adapter.cancelDiscovery();
		}
		else
		{
			otherDevAdapter.clear();
			adapter.startDiscovery();
			registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		/*
		int id = item.getItemId();
		
		if (id == R.id.action_settings)
		{
			// start settings intent
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			
			String name = preferences.getString("prefScreenName", "ClassChatter");
			String time = preferences.getString("prefDiscoverableTime", "120");
			
			if(time.equals("0"))
			{
				Toast.makeText(getApplicationContext(), "Hello " + name + "!\n"
						+ "After clicking \"Go Visible\", you will currently be indefinitely discoverable.", Toast.LENGTH_SHORT).show();
			}
			
			Toast.makeText(getApplicationContext(), "Hello " + name + "!\n"
					+ "After clicking \"Go Visible\", you will currently be discoverable for "+ time + " seconds.", Toast.LENGTH_SHORT).show();			
			
			return true;
		}
		*/
		return super.onOptionsItemSelected(item);
	}
}

