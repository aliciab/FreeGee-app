/** @author Seth Shelnutt
 * @License GPLv3 or later
 * All source code is released free and openly by Seth Shelnutt under the terms of GPLv3 or later, 2013 
 * */

package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/*import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;*/

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import edu.shell.freegee.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.graphics.PorterDuff;

public class FreeGee extends Activity implements OnClickListener {
   
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button startBtn;
    private Button restoreBtn;
    private Button utilBtn;
    private boolean sblopen = false;
    private Device myDevice;
    private String saveloc;
    
    private String DEVICE_XML =   "/sdcard/" +"freegee/devices.xml";
    private String DEVICES_SERVER = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/devices.xml";
    
    private static ProgressDialog mProgressDialog;
    public static boolean isSpecial;
    private ArrayList<Object> mButtons = new ArrayList<Object>();
    private static final String JSON_CACHE_KEY = "freegee_json";

    private ArrayList<Device> DeviceList;
    
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date date = new Date();
    private String now = dateFormat.format(date);
    
    private static boolean mMainActivityActive;
    public static final String EXTRA_FINISHED_DOWNLOAD_ID = "download_id";
    public static final String EXTRA_FINISHED_DOWNLOAD_PATH = "download_path";
    
    private Properties buildProp = new Properties();
    
    private int actionsleft = 0;
    private Action mainAction;
    
    private String LOG_TAG = "Freegee";
    
    
    @Override
    public void onResume(){
    	super.onResume();
        mMainActivityActive = true;
    	checkForDownloadCompleted(getIntent());
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivityActive = false;
        setContentView(R.layout.activity_freegee);
    	File freegeef=new File( "/sdcard"+"/freegee");
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
		if (!RootTools.isAccessGiven()) {
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		if(getBatteryLevel() < 15.0)
			alertbuilder("Error!","Your batter is too low to do anything, please charge it or connect an ac adapter","OK",1);
		
	    // read the property text  file
		File file = new File("/system/build.prop");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException f) {
			CommandCapture command = new CommandCapture(0,"mount -o remount,rw /system");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				Log.e(LOG_TAG,"Can't remount /system");
				alertbuilder("Error!","Can't remount /system","Ok",1);
			} catch (TimeoutException e) {
				Log.e(LOG_TAG,"Chmod timed out");
				alertbuilder("Error!","remount timed out","Ok",1);
			} catch (RootDeniedException e) {
				Log.e(LOG_TAG,"No root access!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
			command = new CommandCapture(0,"chmod 644 /system/build.prop");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				Log.e(LOG_TAG,"");
				alertbuilder("Error!","Can't chmod build.prop","Ok",1);
			} catch (TimeoutException e) {
				Log.e(LOG_TAG,"Chmod timed out");
				alertbuilder("Error!","Chmod timed out","Ok",1);
			} catch (RootDeniedException e) {
				Log.e(LOG_TAG,"No root access!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
			command = new CommandCapture(0,"mount -o remount,ro /system");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				Log.e(LOG_TAG,"Can't remount /system");
				alertbuilder("Error!","Can't remount /system","Ok",1);
			} catch (TimeoutException e) {
				Log.e(LOG_TAG,"remount timed out");
				alertbuilder("Error!","remount timed out","Ok",1);
			} catch (RootDeniedException e) {
				Log.e(LOG_TAG,"No root access!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
		}

		// feed the property with the file
		try {
			buildProp.load(fis);
		} catch (IOException e) {
			alertbuilder("Error!","Can't load build.prop make sure you have root and perms are set correctly","Ok",0);
			e.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException e) {
			alertbuilder("Error!","Can't close build.prop, something went wrong.","Ok",0);
			e.printStackTrace();
		}
		
		if(!new File("/data/data/edu.shell.freegee/edifier").exists()){
		  InputStream in = null;
		  OutputStream out = null;
		  try {
			// read this file into InputStream
			in = getAssets().open("edifier");
	 
			// write the inputStream to a FileOutputStream
			out = new FileOutputStream(new File("/data/data/edu.shell.freegee/edifier"));
			int read = 0;
			byte[] bytes = new byte[50468];
	 
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}	 
		  } catch (IOException e) {
			Log.e(LOG_TAG,"Edifier not found in assets");
			e.printStackTrace();
		  } finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					// outputStream.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	 
			  }
		  }
		}
		
		if(!new File("/data/data/edu.shell.freegee/keys").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("keys");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/data/data/edu.shell.freegee/keys"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				Log.e(LOG_TAG,"Keys not found in assets");
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		if(!new File("/data/data/edu.shell.freegee/mkbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("mkbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/data/data/edu.shell.freegee/mkbootimg"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				Log.e(LOG_TAG,"mkbootimg not found in assets");
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		if(!new File("/data/data/edu.shell.freegee/unpackbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("unpackbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/data/data/edu.shell.freegee/unpackbootimg"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				Log.e(LOG_TAG,"unpackbootimg not found in assets");
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		if(!new File("/data/data/edu.shell.freegee/busybox").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("busybox");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/data/data/edu.shell.freegee/busybox"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				Log.e(LOG_TAG,"busybox not found in assets");
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		CommandCapture command = new CommandCapture(0,"chmod 755 /data/data/edu.shell.freegee/edifier");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e(LOG_TAG,"Edifier not found in assets");
			alertbuilder("Error!","Can't open edifier","Ok",1);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			Log.e(LOG_TAG,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,"chmod 644 /data/data/edu.shell.freegee/keys");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e(LOG_TAG,"keys not found in assets");
			alertbuilder("Error!","Can't open keys","Ok",1);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			Log.e(LOG_TAG,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,"chmod 755 /data/data/edu.shell.freegee/mkbootimg");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e(LOG_TAG,"mkbootimg not found in assets");
			alertbuilder("Error!","Can't open mkbootimg","Ok",1);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			Log.e(LOG_TAG,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,"chmod 755 /data/data/edu.shell.freegee/unpackbootimg");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e(LOG_TAG,"unpackbootimg not found in assets");
			alertbuilder("Error!","Can't open unpackbootimg","Ok",1);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			Log.e(LOG_TAG,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,"chmod 755 /data/data/edu.shell.freegee/busybox");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e(LOG_TAG,"busybox not found in assets");
			alertbuilder("Error!","Can't open busybox","Ok",1);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			Log.e(LOG_TAG,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,"chmod 755 /data/data/edu.shell.freegee/edifier");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e(LOG_TAG,"Edifier not found in assets");
			alertbuilder("Error!","Can't open edifier","Ok",1);
		} catch (TimeoutException e) {
			Log.e(LOG_TAG,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			Log.e(LOG_TAG,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}

		
		/*if(!spiceManager.isStarted())
           spiceManager.start( this );*/
        getDevices();
       
        checkForDownloadCompleted(getIntent());
        
/*		myDevice = new Device();
		myDevice.setName("LG Optimus G");
		ArrayList<Action> actions = new ArrayList<Action>();
		Action unlock = new Action();
		unlock.setName("unlock");
		actions.add(unlock);
		
		Action recovery = new Action();
		recovery.setName("recovery");
		actions.add(recovery);
		
		myDevice.setActions(actions);
		updateGridView(myDevice);*/
		GridView gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setAdapter(new ButtonAdapter(mButtons));
    }
    
    public void updateGridView(Device device){
    	//Toast.makeText(this, "Updating Grid View", Toast.LENGTH_LONG).show();
        for(int i = 0; i < device.getActions().size();i++){
    		Button cb = new Button(this);
  		    cb.setText(device.getActions().get(i).getName());
  		    if(i % 2 == 1){
  		      cb.getBackground().setColorFilter(Color.parseColor("#f47321"), PorterDuff.Mode.DARKEN);
  		      cb.setTextColor(Color.parseColor("#005030"));
            }
  		    else{
  		      cb.getBackground().setColorFilter(Color.parseColor("#005030"), PorterDuff.Mode.DARKEN);
  		      cb.setTextColor(Color.parseColor("#f47321"));
            }
  		    cb.setTypeface(null, Typeface.BOLD);
  		    cb.setPadding(100, 100, 100, 100);
  		    cb.setOnClickListener(this);
  		    cb.setId(i);
  		    mButtons.add(cb);
        }
		GridView gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setAdapter(new ButtonAdapter(mButtons));
    }
    
    public void unSerializeDevices(){
    	//Toast.makeText(this, "Unserializing Devices", Toast.LENGTH_LONG).show();
    	Serializer serializer = new Persister();
    	File source = new File(DEVICE_XML);
    	try {
			DeviceList = serializer.read(Devices.class, source).getDevices();
		} catch (Exception e) {
			Log.e(LOG_TAG,"Could not unserialize");
			alertbuilder("Error!","Could not unserialize devices from xml","Ok",1);
		}
    }
    /*public void unMarshDevices() {
    	JAXBContext jcontext = null;
		try {
			jcontext = JAXBContext.newInstance(FreeGee.class);
		} catch (JAXBException e) {
			Log.e(LOG_TAG,"Could not get JAXB context");
			alertbuilder("Error!","Could not get JAXB context","Ok",1);
		}
    	Unmarshaller um = null;
		try {
			um = jcontext.createUnmarshaller();
		} catch (JAXBException e) {
			Log.e(LOG_TAG,"Could not create unmarshaller for xml reading");
			alertbuilder("Error!","Could not create unmarshaller for xml reading ","Ok",1);
		}
        try {
			DeviceList = (ArrayList<Device>) um.unmarshal(new FileReader(DEVICE_XML));
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG,"Could open Devices file");
			alertbuilder("Error!","Could not open Devices.xml","Ok",1);
		} catch (JAXBException e) {
			Log.e(LOG_TAG,"Could not get JAXB");
			alertbuilder("Error!","Could not get JAXB","Ok",1);
		}
    }*/
    
    @Override
    public void onClick(View v) {
     Button selection = (Button)v;
     //Toast.makeText(getBaseContext(), selection.getText()+ " was pressed!", Toast.LENGTH_SHORT).show();
     for(final Action a:myDevice.getActions()){
    	 if(a.getName().equals(selection.getText())){
    	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        	    
    	    	// set title
    	    	alertDialogBuilder.setTitle(a.getName());

    	    	// set dialog message
    	    	alertDialogBuilder
    	    	.setMessage(a.getDescription())
    	    	.setCancelable(false)
    	    	.setPositiveButton("Proceed",new DialogInterface.OnClickListener() {
    	    	public void onClick(DialogInterface dialog,int id) {
    	    		 mainAction = a;
    	    	     processAction(a);
    	    	}
    	    	})
    	    	.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
        	    	public void onClick(DialogInterface dialog,int id) {
        	    		Toast.makeText(getBaseContext(), "Action" + a.getName() + " was cancelled", Toast.LENGTH_SHORT).show();
       	    	}
    	    	});

    	    	// create alert dialog
    	    	AlertDialog alertDialog = alertDialogBuilder.create();

    	    	// show it
    	    	alertDialog.show();
    	    	break;
    	 }
     }
     //Toast.makeText(getBaseContext(), "Result is: "+result, Toast.LENGTH_SHORT).show();
    }
    
    public class ButtonAdapter extends BaseAdapter {  
    	private ArrayList<Object> mButtons = null;

    	public ButtonAdapter(ArrayList<Object> b) {
    	 mButtons = b;
    	} 
         
        // Total number of things contained within the adapter  
        public int getCount() {  
         return mButtons.size();  
        }  
         
         // Require for structure, not really used in my code.  
        public Object getItem(int position) {  
         return (Object) mButtons.get(position);
        }  
         
        // Require for structure, not really used in my code. Can  
        // be used to get the id of an item in the adapter for   
        // manual control.   
        public long getItemId(int position) {  
         return position;  
        }  

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
        Button button;
        if (convertView == null) {
         button = (Button) mButtons.get(position);
        } else {
         button = (Button) convertView;
        }
         return button;
        }
       }
    
   // protected SpiceManager spiceManager = new SpiceManager( JacksonSpringAndroidSpiceService.class );
    
/*    private class FreegeeRequestListener implements RequestListener< Device >{

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
          //update your UI
        }

        @Override
        public void onRequestSuccess( Device device ) {
        	updateGridView(device);
        }
    }*/


    @Override
    protected void onStart() {
        super.onStart();
        mMainActivityActive = true;
/*		if(!spiceManager.isStarted())
	        spiceManager.start( this );*/
    }

    @Override
    protected void onStop() {
/*    	if(spiceManager.isStarted())
           spiceManager.shouldStop();*/
        mMainActivityActive = false;
        super.onStop();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if we need to refresh the screen to show new updates
/*        if (intent.getBooleanExtra(EXTRA_UPDATE_LIST_UPDATED, false)) {
            updateLayout();
        }*/
        checkForDownloadCompleted(intent);
    }

/*    public void refreshSupported() {
        spiceManager.execute( new FreegeeJsonRequest(), JSON_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, new FreegeeRequestListener() );
    }*/
    
    private void checkForDownloadCompleted(Intent intent) {
    	//Toast.makeText(this, "Checking for Completed Downloads", Toast.LENGTH_SHORT).show();
        if (intent == null) {
            return;
        }

        long downloadId = intent.getLongExtra(EXTRA_FINISHED_DOWNLOAD_ID, -1);
        if (downloadId < 0) {
        //	Toast.makeText(this, "Not download", Toast.LENGTH_LONG).show();
            return;
        }

        String fullPathName = intent.getStringExtra(EXTRA_FINISHED_DOWNLOAD_PATH);
        if (fullPathName == null) {
       // 	Toast.makeText(this, "No path given", Toast.LENGTH_LONG).show();
            return;
        }

        String fileName = new File(fullPathName).getName();
        if(fileName.equalsIgnoreCase("devices.xml")){
        	//Toast.makeText(this, "Found XML", Toast.LENGTH_LONG).show();
        	//unMarshDevices();
        	unSerializeDevices();
        	matchDevice();
        }
        	
        // Find the matching preference so we can retrieve the UpdateInfo
        //UpdatePreference pref = (UpdatePreference) mUpdatesList.findPreference(fileName);
        ArrayList<Action> actions = myDevice.getActions();
        Action thisAction;
        for(Action i:actions){
        	if (i.getZipFile().equalsIgnoreCase(fileName))
            	doAction(i,fullPathName);
        }
    }
    
    public void matchDevice(){
    	//Toast.makeText(this, "Matching Device", Toast.LENGTH_LONG).show();
    	for(Device device:DeviceList){
    		String prop = buildProp.getProperty(device.getProp_id());
    		String prop2 = buildProp.getProperty(device.getProp_id().toLowerCase(Locale.US));
    		String model = device.getModel();
    		if(prop != null){
    			if(prop.equalsIgnoreCase(model)){
    				myDevice = device;
    				break;
    			}
    		}
    		if(prop2 != null){
    			if(prop2.equalsIgnoreCase(model)){
    				myDevice = device;
    				break;
    			}
    		}
    	}
    	if(myDevice != null){
    		updateGridView(myDevice);
    	}
    	else{
    		alertbuilder("Unsupported", "Your devices is not currently supported", "ok", 1);
    	}
    }
    
    public boolean doAction(Action i, String fullPathName){

		 CommandCapture command = new CommandCapture(0,"/data/data/edu.shell.freegee/edifier "+ "/sdcard/freegee/"+i.getZipFile()){
	        @Override
	        public void output(int id, String line)
	        {
	            RootTools.log(LOG_TAG, line);
	        }
		 };
			try {
				RootTools.debugMode = true; //ON
				Shell shell = RootTools.getShell(true);
				shell.add(command);
				commandWait(command);
				int err = command.getExitCode();
				Log.v(LOG_TAG,"Exit code is: " + err);
				if(err == 0){
					actionsleft--;
					Log.v(LOG_TAG,"actionsleft is: " + actionsleft);
					if(actionsleft == 0)
						alertbuilder("Done","The requested action of " + mainAction.getName() +" is complete","Ok",0);
					return true;
				}
				else{
					Toast.makeText(this, "Erroe code is: " + err, Toast.LENGTH_LONG).show();
					actionsleft--;
					alertbuilder("Error","There was an error running action " + i.getName(),"ok",0);
					return false;
				}
				
			} catch (IOException e) {
				Log.e(LOG_TAG,"Edifier not found");
				alertbuilder("Error","Edifier not found.","ok",0);
			} catch (TimeoutException e) {
				Log.e(LOG_TAG,"command timed out");
				alertbuilder("Error","Edifier "+i.getName() + " command timed out","ok",0);
			} catch (RootDeniedException e) {
				Log.e(LOG_TAG,"No root access!");
				alertbuilder("Error","Please check root access","ok",0);
			} catch (Exception e) {
				Log.e(LOG_TAG,"Exception thrown waiting for command to finish");
				alertbuilder("Error","Exception thrown waiting for command to finish","ok",0);
			}
			return false;
    }
    
    private void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e(LOG_TAG, "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }
    
    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f; 
    }
    
    public void processAction(Action action){
		actionsleft++;
    	if(action.getDependencies() != null && !action.getDependencies().isEmpty()){
    		for(Action i:action.getDependencies()){
    			processAction(i);
    		}
    	}
    	startDownload(action);
    }
    
    public void startDownload(Action action){
        Intent intent = new Intent(this, DownloadReceiver.class);
        intent.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
        intent.putExtra(DownloadReceiver.DEVICE_ACTION, (Serializable) action);
        sendBroadcast(intent);    	
    }
    
    public void getDevices(){
    	//Toast.makeText(this, "Getting Supported Devices", Toast.LENGTH_LONG).show();
        Action dAction = new Action();
        dAction.setName("devices.xml");
        dAction.setZipFile("devices.xml");
        dAction.setZipFileLocation("devices.xml");
        dAction.setMd5sum("nono");
        File devicesXML = new File(DEVICE_XML);
        if(devicesXML.exists()){
        	devicesXML.delete();
        }
        startDownload(dAction);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_free_gee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
        		Intent newActivity = new Intent(this, settings.class);
                startActivity(newActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                setmProgressDialog(new ProgressDialog(this));
                getmProgressDialog().setMessage("Downloading file..");
                getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                getmProgressDialog().setCancelable(false);
                getmProgressDialog().show();
                return getmProgressDialog();
		case DIALOG_INSTALL_PROGRESS:
                setmProgressDialog(new ProgressDialog(this));
                getmProgressDialog().setMessage("Installing..");
                getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
                getmProgressDialog().setCancelable(false);
                getmProgressDialog().show();
                return getmProgressDialog();
		case DIALOG_BACKUP_PROGRESS:
            setmProgressDialog(new ProgressDialog(this));
            getmProgressDialog().setMessage("Backing Up..");
            getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
            getmProgressDialog().setCancelable(false);
            getmProgressDialog().show();
            return getmProgressDialog();
		case DIALOG_RESTORE_PROGRESS:
			setmProgressDialog(new ProgressDialog(this));
            getmProgressDialog().setMessage("Restoring..");
            getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
            getmProgressDialog().setCancelable(false);
            getmProgressDialog().show();
            return getmProgressDialog();
		default:
                return null;
        }
    }

    public void alertbuilder(String title, String text, String Button, final int exits){
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	    
    	// set title
    	alertDialogBuilder.setTitle(title);

    	// set dialog message
    	alertDialogBuilder
    	.setMessage(text)
    	.setCancelable(false)
    	.setPositiveButton(Button,new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    	// if this button is clicked, close
    	// current activity
    		if(exits==1){
    			FreeGee.this.finish();
    			}
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}

	public static ProgressDialog getmProgressDialog() {
		return mProgressDialog;
	}
	public void setmProgressDialog(ProgressDialog mProgressDialog) {
		this.mProgressDialog = mProgressDialog;
	}
	public static boolean isMainActivityActive() {
	    return mMainActivityActive;
	    
	}
}