
package com.example.bluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.example.bluetoothtest.R;
import android.support.v7.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity
{
	public static final java.util.UUID MY_UUID
    = java.util.UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");

	int MESSAGE_READ = 16;
	int REQUEST_ENABLE_BT = 1;
	
	ArrayList<String> devices = new ArrayList<String>();
	ArrayAdapter<String> devAdapter;	
	ArrayList<String> messages = new ArrayList<String>();
	ArrayAdapter<String> msgAdapter;	
	ArrayList<String> statuses = new ArrayList<String>();
	ArrayAdapter<String> sAdapter;
	
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	BluetoothDevice test;
	Handler mHandler;
	ConnectedThread bThread;
	boolean connected = false;
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
	    {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action))
	        {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            test = device;
	            // Add the name and address to an array adapter to show in a ListView
	            devices.add(device.getName() + "\n" + device.getAddress());
	            devAdapter.notifyDataSetChanged();
	        }	        
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler();
		
		ListView blv = (ListView) findViewById(R.id.deviceList);
		devAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);
		blv.setAdapter(devAdapter);
		
		ListView mlv = (ListView) findViewById(R.id.messageList);
		msgAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
		mlv.setAdapter(msgAdapter);
		
		ListView slv = (ListView) findViewById(R.id.statusList);
		sAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, statuses);
		slv.setAdapter(sAdapter);
		
		final Button sButton = (Button) findViewById(R.id.buttonServer);
        final Button cButton = (Button) findViewById(R.id.buttonClient);

        sButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	statuses.add("Accept Thread created");
            	sAdapter.notifyDataSetChanged();
            	AcceptThread at = new AcceptThread();
            	at.start();
				//cButton.setVisibility(View.INVISIBLE);
				//sButton.setVisibility(View.INVISIBLE);
            }
        });
        
        cButton.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				if (test != null)
				{
					statuses.add("Connect Thread created for " + test.getName());
	            	sAdapter.notifyDataSetChanged();
					ConnectThread ct = new ConnectThread(test);
					ct.start();
				}
				else
				{
					statuses.add("Connect Thread could not be created");
	            	sAdapter.notifyDataSetChanged();
				}
				//cButton.setVisibility(View.INVISIBLE);
				//sButton.setVisibility(View.INVISIBLE);
			}
		});
        
        final Button mButton = (Button) findViewById(R.id.buttonMessage);
        mButton.setOnClickListener(new View.OnClickListener()
        {	
			@Override
			public void onClick(View v)
			{
				while (mHandler.hasMessages(MESSAGE_READ))
				{
					Message temp = mHandler.obtainMessage();
					String msg = (String) temp.obj;
					messages.add(msg);
					msgAdapter.notifyDataSetChanged();
					statuses.add("Message received: " + msg);
	            	sAdapter.notifyDataSetChanged();
	            }
	            
			}
		});
		
        final Button sendButton = (Button) findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener()
        {	
			@Override
			public void onClick(View v)
			{
				if (connected)
				{
					statuses.add("Bytes sent");
	            	sAdapter.notifyDataSetChanged();
	            	bThread.write("Hello there".getBytes());
				}
			}
		});
		        
		if (mBluetoothAdapter == null)
		{
			statuses.add("Bluetooth could not be found");
        	sAdapter.notifyDataSetChanged();
			return;
		}
		if (!mBluetoothAdapter.isEnabled())
		{
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
		
		devices.add("Available Devices");
		mBluetoothAdapter.startDiscovery();
		statuses.add("Started discovery");
    	sAdapter.notifyDataSetChanged();
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Whyyy", MY_UUID);
	        }
	        catch (IOException e) {
	        	//statuses.add("Error: Unable to connect, " + e.getMessage());
            	//sAdapter.notifyDataSetChanged();
            	//final Button sButton = (Button) findViewById(R.id.buttonServer);
                //final Button cButton = (Button) findViewById(R.id.buttonClient);
				//cButton.setVisibility(View.VISIBLE);
				//sButton.setVisibility(View.VISIBLE);
	        }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true)
	        {
	            try {
	            	socket = mmServerSocket.accept();
	            } catch (IOException e) {
	            	//statuses.add("Error: Unable to connect, " + e.getMessage());
	            	//sAdapter.notifyDataSetChanged();
	            	break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	            	//statuses.add("Connected to " + socket.getRemoteDevice().getName());
	            	//sAdapter.notifyDataSetChanged();
	            	bThread = new ConnectedThread(socket);
	            	bThread.start();
	                connected = true;
	                try {
	                	//statuses.add("Trying to close server socket");
		            	//sAdapter.notifyDataSetChanged();
						mmServerSocket.close();
					} catch (IOException e) {
						//statuses.add("Error: Could not close socket, " + e.getMessage());
						//sAdapter.notifyDataSetChanged();
					}
	                break;
	            }
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	}

	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e){ 
	        	//statuses.add("Error: Unable to create socket, " + e.getMessage());
            	//sAdapter.notifyDataSetChanged();
            }
	        mmSocket = tmp;
	        //statuses.add("Socket created for " + tmp.getRemoteDevice().getName());
	        //sAdapter.notifyDataSetChanged();
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        }
	        catch (IOException connectException)
	        {
	            // Unable to connect; close the socket and get out
	        	//statuses.add("Error: Unable to connect, " + connectException.getMessage());
            	//sAdapter.notifyDataSetChanged();
            	//final Button sButton = (Button) findViewById(R.id.buttonServer);
                //final Button cButton = (Button) findViewById(R.id.buttonClient);
				//cButton.setVisibility(View.VISIBLE);
				//sButton.setVisibility(View.VISIBLE);
            	try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        bThread = new ConnectedThread(mmSocket);
	        bThread.start();
	        connected = true;
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {            	
	            mmSocket.close();
	            connected = false;
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	            connected = false;
	        } catch (IOException e) { }
	    }
	}
	
}
