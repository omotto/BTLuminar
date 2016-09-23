package com.example.bluetoothluminar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class FileListActivity  extends ListActivity {
    private File currentDir;
    private FileArrayAdapter adapter;
    private Stack<File> dirStack = new Stack<File>();
    
    private Option fd;   
    public static final String SOURCE = "/sdcard/";
	
    // Return Intent 
	public static String CHANNELS = "channel_values";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove File when long-press is done
        // Add a listener
 	   	final Activity activity = (Activity) this;
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
            	fd = adapter.getItem(pos);
            	// REMOVE DIALOG 
            	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            	// Add the buttons
            	builder.setTitle("Remove Files");
            	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int id) {
    	       			File file = new File(currentDir, fd.getName());
						file.delete();
						adapter.remove(fd);
						adapter.notifyDataSetChanged();	
            		}
            	});
            	builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int id) {
            			// User cancelled the dialog
            		}
            	});
            	// Create the AlertDialog
            	AlertDialog dialog = builder.create();
            	dialog.show();
            	dialog.setTitle("Remove Files");
				return true;
            }       
        });
        currentDir = new File(SOURCE); //Environment.getExternalStorageDirectory().getPath()
	    fill(currentDir);
    }
	        
    private void fill(File f) {
    	File[]dirs = f.listFiles();
		this.setTitle("Current Dir: "+f.getName());
		List<Option>dir = new ArrayList<Option>();
		List<Option>fls = new ArrayList<Option>();
		try{
			for(File ff: dirs) {
				if(ff.isDirectory()) {
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				} else {
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
		} catch(Exception e) {
			Log.d("FileChooser", "Error: " + e);
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		
		if (!f.getName().equalsIgnoreCase("sdcard")) dir.add(0,new Option("..","Parent Directory",f.getParent()));
		adapter = new FileArrayAdapter(this,R.layout.file_list, dir);
		this.setListAdapter(adapter);
    }
	    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")) {
			dirStack.push(currentDir);
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			if (o.getData().equalsIgnoreCase("parent directory")) {
				currentDir = dirStack.pop();
				fill(currentDir);
			} else {
				onFileClick(o);
			}
		}
	}

    private void onFileClick(Option o) {
    	Log.d("FILE","Carpeta actual: "+currentDir);
    	if (o.getName().endsWith(".spm")) {
    		Toast.makeText(this, "File " + currentDir + "/" + o.getName() + " loaded", Toast.LENGTH_SHORT).show();
	   		// Load file
	   		try {
	   		    int[] buffer = new int[13]; // 13 channels
	   			BufferedReader br = new BufferedReader(new FileReader(currentDir+"/"+o.getName()));
	 		    String line = br.readLine();
	 		    int c = 0;
		        while (line != null) {
	            	Log.d("Read File", "Reading: "+ Integer.valueOf(line));
	            	buffer[c] = Integer.valueOf(line);
			    	c++;
			    	// -- Read New Line
		        	line = br.readLine();
		        }
		        br.close();
		        // Create the result Intent and include the channel value
 		        Intent intent = new Intent();
 		        // Save channels
		        intent.putExtra(CHANNELS, buffer);
		        // Set OK
                setResult(RESULT_OK, intent);
                // Finish the activity
                finish();
			} catch (Exception e) {
				e.printStackTrace();
			    Log.d("Read File", "Error: " + e);
			}
	    } else {
	    	Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
	    }
	}

}
