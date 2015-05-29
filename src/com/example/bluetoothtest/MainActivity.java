package com.example.bluetoothtest;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

// Bluetooth implementation based on http://examples.javacodegeeks.com/android/core/bluetooth/bluetoothadapter/android-bluetooth-example/
public class MainActivity extends Activity {
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
	private Button buttonSubmit;
	private SeekBar sb1, sb2, sb3, sb4, sb5, sb6, sb7, sb8;
	private int c1, c2, c3, c4, c5, c6, c7, c8;
	private String n1 = "a", n2 = "b", n3 = "c",
			n4 = "d", n5 = "e", n6 = "f",
			n7 = "g", n8 = "h";

	// COMMUNICATION VARIABLES
	private BluetoothConnection connection;
	private String mConnectedDeviceName = null;
	private BluetoothAdapter adapter;
	final Context context = this;
	private BluetoothDevice dev;

	public static final int NUM_CATS = 8;
	public static final int UPDATE_INTERVAL = 30000; // how often pick cat

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		adapter = BluetoothAdapter.getDefaultAdapter();

		sb1 = (SeekBar) findViewById(R.id.sb1);
		sb2 = (SeekBar) findViewById(R.id.sb2);
		sb3 = (SeekBar) findViewById(R.id.sb3);
		sb4 = (SeekBar) findViewById(R.id.sb4);
		sb5 = (SeekBar) findViewById(R.id.sb5);
		sb6 = (SeekBar) findViewById(R.id.sb6);
		sb7 = (SeekBar) findViewById(R.id.sb7);
		sb8 = (SeekBar) findViewById(R.id.sb8);

		sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c1 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c2 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c3 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c4 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c5 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c6 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb7.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c7 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sb8.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				c8 = bar.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		class UpdateTask extends TimerTask {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					public void run() {
						String name;
						int max = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8;
						Random r = new Random();
						if (max == 0) {
							int val = r.nextInt() % 8;
							switch (val) {
							case 0:
								name = n1;
							case 1:
								name = n2;
							case 2:
								name = n3;
							case 3:
								name = n4;
							case 4:
								name = n5;
							case 5:
								name = n6;
							case 6:
								name = n7;
							default:
								name = n8;
							}
						} else {
							int val = r.nextInt(max);
							if (val < c1)
								name = n1;
							else if (val < c1 + c2)
								name = n2;
							else if (val < c1 + c2 + c3)
								name = n3;
							else if (val < c1 + c2 + c3 + c4)
								name = n4;
							else if (val < c1 + c2 + c3 + c4 + c5)
								name = n5;
							else if (val < c1 + c2 + c3 + c4 + c5 + c6)
								name = n6;
							else if (val < c1 + c2 + c3 + c4 + c5 + c6 + c7)
								name = n7;
							else
								name = n8;
						}
						name = "HM01" + adapter.getAddress() + name;
						adapter.setName(name);
						/*
						Toast t = Toast.makeText(context, name,
								Toast.LENGTH_LONG);
						t.show();
						*/
					}
				});
			}
		}
		;
		Timer t = new Timer();
		t.schedule(new UpdateTask(), UPDATE_INTERVAL, UPDATE_INTERVAL);

		/*
		buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
		buttonSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String name = String.valueOf(c1) + "-" + String.valueOf(c2)
						+ "-" + String.valueOf(c3) + "-" + String.valueOf(c4)
						+ "-" + String.valueOf(c5) + "-" + String.valueOf(c6)
						+ "-" + String.valueOf(c7) + "-" + String.valueOf(c8);

				Toast t = Toast.makeText(context, name, Toast.LENGTH_LONG);
				t.show();
			}
		});
		*/
		
		if (adapter == null) {
			// alert user of incompatibility before closing (finishing) app
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Bluetooth is not supported on your device!")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			// make discoverable forever
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		if (!adapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		connection = new BluetoothConnection(this, mHandler);
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (connection != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (connection.getState() == BluetoothConnection.STATE_NONE) {
				connection.start();
			}
		}
	}

	public void setDevice(BluetoothDevice device) {
		dev = device;
	}

	public BluetoothDevice getDevice() {
		return dev;
	}

	protected void setupConnection(View v, int position,
			ArrayAdapter<String> arrayAdapter) {
		// obtain MAC address of clicked device
		String deviceDescriptor = arrayAdapter.getItem(position);
		int length = deviceDescriptor.length();
		String mac = deviceDescriptor.substring(length - 17, length);

		// reconstruct device using MAC address
		BluetoothDevice device = adapter.getRemoteDevice(mac);
		setDevice(device);

		String name = device.getName();
		if (name == null) {
			Toast.makeText(getApplicationContext(),
					"This device is nowhere to be found!", Toast.LENGTH_SHORT)
					.show();
		} else {
			AlertDialog.Builder adb = new AlertDialog.Builder(context);
			adb.setMessage("Connect to " + name + " ?")
					.setCancelable(false)
					.setPositiveButton("Accept",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									connection.connect(getDevice(), false);
								}
							})
					.setNegativeButton("Decline",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog a = adb.create();
			a.show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (connection != null)
			connection.stop();
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				Intent intent_write = new Intent(getApplicationContext(),
						BluetoothMessage.class);
				intent_write.putExtra("source", "Me");
				intent_write.putExtra("buf", writeBuf);
				startActivity(intent_write);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Intent intent_read = new Intent(getApplicationContext(),
						BluetoothMessage.class);
				intent_read.putExtra("source", mConnectedDeviceName);
				intent_read.putExtra("buf", readBuf);
				intent_read.putExtra("size", msg.arg1);
				startActivity(intent_read);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				BluetoothMessage.myConnection = connection;
				BluetoothMessage.myAdapter = adapter;
				Intent intent_new = new Intent(getApplicationContext(),
						BluetoothMessage.class);
				startActivity(intent_new);
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	protected void toggleBluetooth(View v) {
		if (!adapter.isEnabled()) {
			Intent intent = new Intent(adapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		} else {
			adapter.disable();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			// if we want to do something
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
