
/* 
 * Code adapted from:
 * http://examples.javacodegeeks.com/android/core/bluetooth/bluetoothadapter/android-bluetooth-example/
 */

package com.example.bluetoothtest;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class BluetoothMessage extends Activity
{
	public static BluetoothConnection myConnection = null;
	public static BluetoothAdapter myAdapter = null;
	public static ActionBar actionBar;
	
    private ListView messageList;
    private Button buttonSend;
    private ArrayAdapter<String> msgAdapter;

	private StringBuffer mOutStringBuffer;	
	private boolean sendingData;
	private boolean expectingData;
	ByteArrayOutputStream outputStream;
	
	public static final String HEADER_START = "{[<CLCH1>]}";
	public static final String HEADER_END = "{[<CLCH2>]}";
	public int dataPackets;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_bluetooth);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		//actionBar = getActionBar();
		
		// reset variables
		sendingData = false;
		expectingData = false;
		outputStream = null;
		dataPackets = 0;
		
		msgAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		messageList = (ListView) findViewById(R.id.messagesList);
        messageList.setAdapter(msgAdapter);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	//TODO: Shouldn't be hard to change this to send any kind of message
                String message = "Hello there...";
                sendMessage(message);
            }
        });

		mOutStringBuffer = new StringBuffer("");
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		  super.onNewIntent(intent);
		  
		  // set getIntent() to return this new intent
		  setIntent(intent);
		  Intent currIntent= getIntent();
		  
		  Bundle extras = currIntent.getExtras();
		  
		  if(extras!=null)
		  {
			  String source = currIntent.getExtras().getString("source");
			  byte[] bytes = currIntent.getExtras().getByteArray("buf");
			  
			  // write message
			  String writeMessage = new String(bytes);
			  
			  // read message:
			  int size = currIntent.getExtras().getInt("size");
			  String message = new String(bytes, 0, size);
			  
			  // if a message was generated...
			  if(source!=null && (message!=null || writeMessage!=null)) {
				  
				  // writing own messages to array adapter
				  if(source.equals("Me"))
				  {  
					  if(writeMessage.contains(HEADER_START))
					  {
						  sendingData = true;
					  }
					  else if(writeMessage.contains(HEADER_END))
					  {
						  sendingData = false;
					  }
					  else if(sendingData)
					  {
						  // do nothing; you're automatically transmitting some data or file
					  }
					  else
					  {
						  // normal message
						  msgAdapter.add(source + ": " + writeMessage);
					  }
				  }
				  // reading received messages to array adapter
				  else {
					  
					  if (!expectingData && message.contains(HEADER_START))
					  {
						  //received wrapper beginning, no data expected yet
						  Log.d("HEADER", message);
						  
						  expectingData = true;
						  outputStream = new ByteArrayOutputStream();
					  }
					  else if(expectingData && message.contains(HEADER_END))
					  {
						  //received wrapper ending, closing expected data
						  Log.d("FOOTER", message);
						  Log.d("DATAPACKETS", ""+dataPackets);
						  dataPackets = 0;
						  
						  msgAdapter.add(outputStream.toString());
						  
						  // reset variables
						  expectingData = false;
						  if (outputStream!=null)
						  {
							  try
							  {
								  outputStream.close();
								  Log.d("HEADER_END", "closed outputstream");
							  } catch (IOException e) {}
						  }
					  }
					  else if (expectingData)
					  {
						  //in the middle of the wrapper, expecting data and adding to outputStream
						  Log.d("DATA", new String(bytes));
						  
						  dataPackets++;
						  outputStream.write(bytes, 0, size);
					  }
					  else if (message.contains(HEADER_START) || message.contains(HEADER_END))
					  {
						  // do nothing (get rid of re-reading excess HEADER_END messages)
					  }
					  else
					  {
						  // normal message
						  msgAdapter.add(source + ": " + message);
					  }
				  }
			  }
			  else
			  {
				  // do nothing (no message)
			  }
		  }
	}
	
    private void sendMessage(String message)
    {
    	// Check that we're actually connected before trying anything
    	if (myConnection.getState() != BluetoothConnection.STATE_CONNECTED)
    	{
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0)
        {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            myConnection.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }	
    }
    
    /*
    private void sendMessage(byte[] bytes)
    {
    	// Check that we're actually connected before trying anything
    	if (myConnection.getState() != BluetoothConnection.STATE_CONNECTED)
    	{
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (bytes != null)
        {
            // Get the message bytes and tell the BluetoothChatService to write
            myConnection.write(bytes);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);            
        }	
    }
    */
    /*
    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener()
    {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
        {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP)
            {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };
    */
    
    @Override
	public void onDestroy()
    {
		super.onDestroy();
		if(myConnection.getState()==BluetoothConnection.STATE_CONNECTED)
		{
			myConnection.endConnection();
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.message, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    	default:
    		break;
    	}
    	return true;
    }
}

