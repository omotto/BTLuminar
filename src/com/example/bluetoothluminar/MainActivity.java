package com.example.bluetoothluminar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYPlot;
import com.example.bluetoothluminar.BluetoothConnection;
import com.example.bluetoothluminar.DeviceListActivity;
import com.example.bluetoothluminar.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
 
public class MainActivity extends Activity {
 
   private static final int REQUEST_CONNECT_DEVICE = 1;
   private static final int REQUEST_ENABLE_BT = 2;
   private static final int REQUEST_CHANNEL_VALUES = 3;

   private BluetoothConnection BTConnection = null;
   private BluetoothAdapter mBluetoothAdapter = null;
    
   private TextView mTitle, text1, text2, text3, text4, text5, text6, text7, text8, text9, text10, text11, text12, text13;
   private SeekBar sk1, sk2, sk3, sk4, sk5, sk6, sk7, sk8, sk9, sk10, sk11, sk12, sk13;
   private String mConnectedDeviceName = null;
   
   private byte[] buffer = new byte[35];
   
   private XYPlot mySimpleXYPlot;
   private XYSeries serie;
   private Number[] y_axis;
   private Number[][] channels = new Number[13][81];
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
	   	super.onCreate(savedInstanceState);
	   	setContentView(R.layout.activity_main);
      
	   	// Get local Bluetooth adapter
	   	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	   // -------------------------------------
	   // Preparamos la trama a enviar
   		buffer[0] = 0x02; // STX
   		buffer[1] = 0x69; // IND
   		for (int c = 2; c < 34; c++) buffer[c] = 0x00; // DATA
   		buffer[34] = 0x03; // ETX
   		// -------------------------------------
      
   		// If the adapter is null, then Bluetooth is not supported
   		if (mBluetoothAdapter == null) {
   			Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
   			finish();
   			return;
   		} else {
   			// Read calibration file
   			int currentLine = 0;
   			String line;    	  
   			BufferedReader br = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.calibracion)));
			try {
				while ((line = br.readLine()) != null) {
					currentLine++;
					if (currentLine > 23) {
						String[] split = line.split("\\s+"); 
						for(int i = 1; i < split.length; i++) {
							channels[i-1][currentLine-24] = Float.parseFloat(split[i]);
							//Log.d("FILE", "Value " + i + ": " + channels[i-1][currentLine-24]/*split[i]*/);
						}
					}
				}
			} catch (IOException e1) {
				Log.d("FILE","ERROR: "+e1);
				e1.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				Log.d("FILE","ERROR: "+e);					
				e.printStackTrace();
			}
    	  
			// -----------------
			// GraphWindow
			// -----------------		
			// initialize our XYPlot reference:
			mySimpleXYPlot = (XYPlot)findViewById(R.id.mySimpleXYPlot);
			// Setting Y axis
			y_axis = new Number[81];
			// Setting X axis
			Number[] x_axis = new Number[81];
			for (int c = 0; c < 81; c++) {
				y_axis[c] = 380+c*5;
				x_axis[c] = 0;
			}
			// seriesNumbers = NumberFormat.parse();		
			// Turn the above arrays into XYSeries':
			serie = new SimpleXYSeries(Arrays.asList(y_axis), // SimpleXYSeries takes a List so turn our array into a List
									   Arrays.asList(x_axis),/*SimpleXYSeries.ArrayFormat.Y_VALS_ONLY*/ // Y_VALS_ONLY means use the element index as the x value
								       "Spectro"); // Set the display title of the series
			
			// Create a formatter to use for drawing a series using LineAndPointRenderer:
			LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), // line color
			    														   Color.rgb(0, 100, 0), // point color
																		   null, // fill color (none)
																		   null/*new PointLabelFormatter(Color.WHITE)*/); // text color
			// add a new series' to the xyplot:
			mySimpleXYPlot.addSeries(serie, seriesFormat);
			// same as above:
			//mySimpleXYPlot.addSeries(series2, new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0,100), null, new PointLabelFormatter(Color.WHITE)));
			// reduce the number of range labels
			mySimpleXYPlot.setTicksPerRangeLabel(3);
			
			
   			sk1 = (SeekBar) findViewById(R.id.seekBar1);
   			sk2 = (SeekBar) findViewById(R.id.seekBar2);
   			sk3 = (SeekBar) findViewById(R.id.seekBar3);
   			sk4 = (SeekBar) findViewById(R.id.seekBar4);
   			sk5 = (SeekBar) findViewById(R.id.seekBar5);
   			sk6 = (SeekBar) findViewById(R.id.seekBar6);
   			sk7 = (SeekBar) findViewById(R.id.seekBar7);
   			sk8 = (SeekBar) findViewById(R.id.seekBar8);
   			sk9 = (SeekBar) findViewById(R.id.seekBar9);
   			sk10 = (SeekBar) findViewById(R.id.seekBar10);
   			sk11 = (SeekBar) findViewById(R.id.seekBar11);
   			sk12 = (SeekBar) findViewById(R.id.seekBar12);
   			sk13 = (SeekBar) findViewById(R.id.seekBar13);
   			
   			mTitle = (TextView) findViewById(R.id.text);
   			
   			text1 = (TextView) findViewById(R.id.text1);
   			text2 = (TextView) findViewById(R.id.text2);
   			text3 = (TextView) findViewById(R.id.text3);
   			text4 = (TextView) findViewById(R.id.text4);
   			text5 = (TextView) findViewById(R.id.text5);
   			text6 = (TextView) findViewById(R.id.text6);
   			text7 = (TextView) findViewById(R.id.text7);
   			text8 = (TextView) findViewById(R.id.text8);
   			text9 = (TextView) findViewById(R.id.text9);
   			text10 = (TextView) findViewById(R.id.text10);
    	  	text11 = (TextView) findViewById(R.id.text11);
    	  	text12 = (TextView) findViewById(R.id.text12);
    	  	text13 = (TextView) findViewById(R.id.text13);  
    	  
    	  	text1.setText("Channel 1: 0");
    	  	text2.setText("Channel 2: 0");
    	  	text3.setText("Channel 3: 0");
    	  	text4.setText("Channel 4: 0");
    	  	text5.setText("Channel 5: 0");
    	  	text6.setText("Channel 6: 0");
    	  	text7.setText("Channel 7: 0");
    	  	text8.setText("Channel 8: 0");
    	  	text9.setText("Channel 9: 0");
    	  	text10.setText("Channel 10: 0");
    	  	text11.setText("Channel 11: 0");
    	  	text12.setText("Channel 12: 0");
    	  	text13.setText("Channel 13: 0");
          
    	  	sk1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk1", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk1: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		    	// Print
  		    	text1.setText("Channel 1: ");
                text1.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[2] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[3] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk1", "Data sent");
  		        } 
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
    	  	});

    	  	sk2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk2", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk2: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text2.setText("Channel 2: ");
                text2.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[4] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[5] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk2", "Data sent");
  		        }  
  		        // -- Plot Spectro
  		        PlotSpectro();  		        
  		    }       
    	  	});		
  	
    	  	sk3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk3", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk3: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  		    	
  		    	// Print
  		    	text3.setText("Channel 3: ");
                text3.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[6] = (byte) ((progress >> 8)&0xFF);
		    	buffer[7] = (byte) (progress&0xFF);
		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
		        	BTConnection.write(buffer);
		        	Log.d("sk3", "Data sent");
		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
    	  	});	
  		
    	  	sk4.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk4", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk4: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text4.setText("Channel 4: ");
                text4.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[8] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[9] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk4", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});			
  		
  			sk5.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk5", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk5: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text5.setText("Channel 5: ");
                text5.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[10] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[11] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk5", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});			
  		
  			sk6.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk6", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk6: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text6.setText("Channel 6: ");
                text6.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[12] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[13] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk6", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});			
  		
  			sk7.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk7", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk7: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text7.setText("Channel 7: ");
                text7.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[14] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[15] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk7", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});			
  		
  			sk8.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk8", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk8: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text8.setText("Channel 8: ");
                text8.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[16] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[17] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk8", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});
  		
  			sk9.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk9", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk9: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text9.setText("Channel 9: ");
                text9.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[18] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[19] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk9", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});
  		
  			sk10.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk10", String.valueOf(progress));
  	    		Toast.makeText(getApplicationContext(), "sk10: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text10.setText("Channel 10: ");
                text10.append(String.valueOf(progress));
                // Set frame	
  	    		buffer[20] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[21] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk10", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});	
  		
  			sk11.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk11", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk11: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text11.setText("Channel 11: ");
                text11.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[22] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[23] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk11", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});			
  		
  			sk12.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk12", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk12: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }       
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text12.setText("Channel 12: ");
                text12.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[24] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[25] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk12", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});			

  			sk13.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
  		    @Override       
  		    public void onStopTrackingTouch(SeekBar seekBar) {      
/*  		    	int progress = seekBar.getProgress();
  		    	Log.d("sk13", String.valueOf(progress));
  		    	Toast.makeText(getApplicationContext(), "sk13: "+String.valueOf(progress), Toast.LENGTH_SHORT).show();*/
  		    }  		    
  		    @Override       
  		    public void onStartTrackingTouch(SeekBar seekBar) {}       
  		    @Override       
  		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  		    	// Print
  		    	text13.setText("Channel 13: ");
                text13.append(String.valueOf(progress));
                // Set frame	
  		    	buffer[26] = (byte) ((progress >> 8)&0xFF);
  		    	buffer[27] = (byte) (progress&0xFF);
  		    	// -- Send Data
  		        if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
  		        	BTConnection.write(buffer);
  		        	Log.d("sk13", "Data sent");
  		        }
  		        // -- Plot Spectro
  		        PlotSpectro();
  		    }       
  			});       
   		}
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode) {
       		case REQUEST_CHANNEL_VALUES:
       			if (resultCode == Activity.RESULT_OK) {
       				int [] values = data.getExtras().getIntArray(FileListActivity.CHANNELS);
       				sk1.setProgress(values[0]);
       				sk2.setProgress(values[1]);
       				sk3.setProgress(values[2]);
       				sk4.setProgress(values[3]);
       				sk5.setProgress(values[4]);
       				sk6.setProgress(values[5]);
       				sk7.setProgress(values[6]);
       				sk8.setProgress(values[7]);
       				sk9.setProgress(values[8]);
       				sk10.setProgress(values[9]);
       				sk11.setProgress(values[10]);
       				sk12.setProgress(values[11]);
       				sk13.setProgress(values[12]);
       				for (int c = 0; c < 13; c++) {
       					buffer[c*2+2] = (byte) ((values[c] >> 8) & 0xFF);
       					buffer[c*2+2+1] = (byte) (values[c] & 0xFF); 
       				}
       				if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) 
       					BTConnection.write(buffer);
      		        // -- Plot Spectro
      		        PlotSpectro();
       			}	
       			break;
       		case REQUEST_CONNECT_DEVICE:
	           // When DeviceListActivity returns with a device to connect
	           if (resultCode == Activity.RESULT_OK) {
	               // Get the device MAC address
	               String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	               // Get the BLuetoothDevice object
	               BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	               // Attempt to connect to the device
	               Log.d("connect","connect to: " + device);
	               //--
	               BTConnection = new BluetoothConnection(this, mHandler);
	               //--	               
	               BTConnection.connect(device);
	           }
	           break;
       		case REQUEST_ENABLE_BT:
	           // When the request to enable Bluetooth returns
	           if (resultCode == Activity.RESULT_OK) {
	               // Bluetooth is now enabled, so set up a chat session
	        	   BTConnection = new BluetoothConnection(this, mHandler);
	        	   //setupConnection();
	           } else {
	               // User did not enable Bluetooth or an error occured
	               Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
	               finish();
	           }
       }
   }
   
   // The Handler that gets information back from the BluetoothConnection
   private final Handler mHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
           switch (msg.what) {
           case BluetoothConnection.MESSAGE_STATE_CHANGE:
               Log.i("handler", "MESSAGE_STATE_CHANGE: " + msg.arg1);
               switch (msg.arg1) {
               case BluetoothConnection.STATE_CONNECTED:
                   mTitle.setText(R.string.title_connected_to);
                   mTitle.append(mConnectedDeviceName);
                   //mConversationArrayAdapter.clear();
                   break;
               case BluetoothConnection.STATE_CONNECTING:
                   mTitle.setText(R.string.title_connecting);
                   break;
               case BluetoothConnection.STATE_LISTEN:
               case BluetoothConnection.STATE_NONE:
                   mTitle.setText(R.string.title_not_connected);
                   break;
               }
               break;
           case BluetoothConnection.MESSAGE_WRITE:
               byte[] writeBuf = (byte[]) msg.obj;
               // construct a string from the buffer
               String writeMessage = new String(writeBuf);
               //mConversationArrayAdapter.add("Me:  " + writeMessage);
               break;
           case BluetoothConnection.MESSAGE_READ:
               byte[] readBuf = (byte[]) msg.obj;
               // construct a string from the valid bytes in the buffer
               String readMessage = new String(readBuf, 0, msg.arg1);
               //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
               break;
           case BluetoothConnection.MESSAGE_DEVICE_NAME:
               // save the connected device's name
               mConnectedDeviceName = msg.getData().getString(BluetoothConnection.DEVICE_NAME);
               Toast.makeText(getApplicationContext(), "Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
               break;
           case BluetoothConnection.MESSAGE_TOAST:
               Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConnection.TOAST), Toast.LENGTH_SHORT).show();
               break;
           }
       }
   };
   
   @Override
   protected void onDestroy() {
       super.onDestroy();
       if (BTConnection != null) BTConnection.stop();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.main, menu);
       return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
	       case R.id.scan:
	           // Launch the DeviceListActivity to see devices and do scan
	           Intent serverIntent = new Intent(this, DeviceListActivity.class);
	           startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	           return true;
	       case R.id.connect:
	    	   if (!mBluetoothAdapter.isEnabled()) {
	    		   Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    	       startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
	    	       Toast.makeText(getApplicationContext(),"Bluetooth turned on", Toast.LENGTH_LONG).show();
	    	   } else {
	    		   mBluetoothAdapter.disable();
	    		   mTitle.setText("Status: Disconnected");
	    	       Toast.makeText(getApplicationContext(),"Bluetooth turned off", Toast.LENGTH_LONG).show();    	   
	    	   }
	           return true;
	       case R.id.load:
	    	   // Launch the DeviceListActivity to see devices and do scan
	           Intent loadIntent = new Intent(this, FileListActivity.class);
	           startActivityForResult(loadIntent, REQUEST_CHANNEL_VALUES);
	           return true;	    	   
	       case R.id.save:
	    	   // ---------------------------
	    	   // Create dialog to save file
	    	   // ---------------------------
	    	   final Activity activity = (Activity) this;
	    	   AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    	   // Get the layout inflater
	    	   LayoutInflater dialog_inflater = activity.getLayoutInflater();
	    	   // Inflate and set the layout for the dialog
	    	   // Pass null as the parent view because its going in the dialog layout
	    	   builder.setTitle("Set Spectro Name");
	    	   builder.setView(dialog_inflater.inflate(R.layout.dialog_layout, null))
	    	   // Add action buttons
		           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		        	   @Override
		        	   public void onClick(DialogInterface dialog, int id) {
		        		    Dialog f = (Dialog) dialog;
		                    //This is the input I can't get text from
		                    EditText filename = (EditText) f.findViewById(R.id.filename);
		                    File currentDir = new File("/sdcard/");
	        				Log.d("Save File",currentDir+filename.getText().toString()+".spm"); 
				        	// Save file
	        				//FileOutputStream outputStream;
		        			File file = null;
		        			try {
	        					//outputStream = getActivity().openFileOutput(filename.getText().toString()+".spm", Context.MODE_PRIVATE);
		        				file = new File(currentDir,filename.getText().toString()+".spm"); 
		        			   	//PrintStream printStream = new PrintStream(outputStream);
		        			   	PrintStream printStream = new PrintStream(file);
		        			   	printStream.print(sk1.getProgress()+"\r\n"+sk2.getProgress() +"\r\n"+ sk3.getProgress() +"\r\n"+
		        			   					  sk4.getProgress() +"\r\n"+ sk5.getProgress() +"\r\n"+ sk6.getProgress() +"\r\n"+
		        			   					  sk7.getProgress() +"\r\n"+ sk8.getProgress() +"\r\n"+ sk9.getProgress() +"\r\n"+
		        			   					  sk10.getProgress() +"\r\n"+ sk11.getProgress() +"\r\n"+ sk12.getProgress() +"\r\n"+
		        			   					  sk13.getProgress());
		        			   	printStream.close();
		        			   	//outputStream.close();
		        			  } catch (Exception e) {
		        				 Log.d("Save File","ERROR: "+e);
		        				 e.printStackTrace();
		        			  }
		        	   }
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Exit
						}
					});      
				 builder.create().show();
	    	   return true;
	       case R.id.about:
	    	   View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
	           // When linking text, force to always use default color. This works around a pressed color state bug.
	           TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
	           int defaultColor = textView.getTextColors().getDefaultColor();
	           textView.setTextColor(defaultColor);
	           AlertDialog.Builder my_builder = new AlertDialog.Builder(this);
	           my_builder.setIcon(R.drawable.app_icon);
	           my_builder.setTitle(R.string.app_name);
	           my_builder.setView(messageView);
	           my_builder.create();
	           my_builder.show();
	    	   return true;
       }
       return false;
   }

   void PlotSpectro()
   {	// -- Calculate new spectro
	    int value = 0;
	    Number[] x_axis = new Number[81];
	    for (int c = 0; c < 81; c++) x_axis[c] = 0;
	    for (int i = 0; i < 13; i++) {
			value = (buffer[2+i*2] & 0x0ff);
			value = (value << 8);
			value =  value + ((buffer[3+(i*2)]) & 0xff);
			for (int j = 0; j < 81; j++) {
   			x_axis[j] = x_axis[j].floatValue() + (channels[i][j].floatValue() * value);
			}
	    }
        // -- Plot
		mySimpleXYPlot.clear();
		serie = new SimpleXYSeries(Arrays.asList(y_axis), Arrays.asList(x_axis), "Spectro"); 
		LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, null);
		mySimpleXYPlot.addSeries(serie, seriesFormat);
		mySimpleXYPlot.redraw(); 
   }
}
