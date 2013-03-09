package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import edu.shell.freegee.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

@SuppressLint("SdCardPath")
public class FreeGee extends Activity {
   
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button startBtn;
    private Button restoreBtn;
    private Button efsBtn;
    private ProgressDialog mProgressDialog;
    private String varient;
    private boolean restoring;
    private boolean override;
   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_gee);
    	File freegeef=new File("/sdcard/freegee");
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = FreeGee.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("By Pressing Okay you are acknowledging that you are voiding you are voiding you warrenty and no one from team codefire can be held responsible.")
                	.setCancelable(false)
                	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	
                		Intent newActivity = new Intent(getBaseContext(), install.class);
                        startActivity(newActivity);
                	}
                	})
                	.setNegativeButton("I disagree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		
                			FreeGee.this.finish();
                			
                	}
                	});

                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();

                	// show it
                	alertDialog.show();
            		
                   
                }
            }
        });
        restoreBtn = (Button)findViewById(R.id.restoreBtn);
        restoreBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = FreeGee.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("By Pressing Okay you are acknowledging that you are returning to a locked state by using the backups made while unlocking.")
                	.setCancelable(false)
                	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		File backup_script=new File("/sdcard/freegee/freegee-restore.sh");
           			  if(!backup_script.exists()){
           				restoring = true; 
           				File file = new File("/system/build.prop");
           				FileInputStream fis = null;
           				try {
           					fis = new FileInputStream(file);
           				} catch (FileNotFoundException e) {
           					// TODO Auto-generated catch block
           					e.printStackTrace();
           				}

           				Properties prop = new Properties();
           				// feed the property with the file
           				try {
           					prop.load(fis);
           				} catch (IOException e) {
           					// TODO Auto-generated catch block
           					e.printStackTrace();
           				}
           				try {
           					fis.close();
           				} catch (IOException e) {
           					// TODO Auto-generated catch block
           					e.printStackTrace();
           				}
           				String recovery = "twrp";
           				String device = prop.getProperty("ro.product.name");
           				if(device.equalsIgnoreCase("geehrc4g_spr_us")){
           					varient = "sprint";
           		        String url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/sprint/private/freegee/freegee-apk-sprint-"+recovery+".tar";
           		        new DownloadFileAsync().execute(url);
           		        }
           				else if(device.equalsIgnoreCase("geeb_att_us")){
           					varient = "att";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/att/private/freegee/freegee-apk-att-"+recovery+".tar");
           				}
           				else if(device.equalsIgnoreCase("geeb_bell_ca")){
           					varient = "bell";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/bell/private/freegee/freegee-apk-bell-"+recovery+".tar");
           				}
           				else if(device.equalsIgnoreCase("geeb_rgs_ca")){
           					varient = "rogers";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/rogers/private/freegee/freegee-apk-rogers-"+recovery+".tar");
           				}
           				else if(device.equalsIgnoreCase("geeb_tls_ca")){
           					varient = "telus";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/telus/private/freegee/freegee-apk-telus-"+recovery+".tar");
           				}
     			          }
           			  else{
                   		 new restore().execute();
           			  }
                	}
                	})
                	.setNegativeButton("I disagree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		
                			FreeGee.this.finish();
                			
                	}
                	});

                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();

                	// show it
                	alertDialog.show();
            		
                   
                }
            }
        });
        efsBtn = (Button)findViewById(R.id.efsBtn);
        efsBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = FreeGee.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("It is highly important to backup your efs partitions. In the event of radio issues these backups might save your device. Do you want to backup or restore efs?")
                	.setCancelable(false)
                	.setPositiveButton("Backup",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                		File efs1=new File("/sdcard/freegee/m9kefs1.img");
                		File efs2=new File("/sdcard/freegee/m9kefs2.img");
                		File efs3=new File("/sdcard/freegee/m9kefs3.img");
                		if(efs1.exists() || efs2.exists() || efs3.exists()){
                			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
                    	    
                        	// set title
                        	alertDialogBuilder.setTitle("Warning");

                        	// set dialog message
                        	alertDialogBuilder
                        	.setMessage("Existing backups detected, do you want to override them?")
                        	.setCancelable(false)
                        	.setPositiveButton("Override",new DialogInterface.OnClickListener() {
                        		public void onClick(DialogInterface dialog,int id) {
                        			override = true;
                              		new efsbackup().execute();
                        		}
                        	})
                        	.setNegativeButton("No",new DialogInterface.OnClickListener() {
                            	public void onClick(DialogInterface dialog,int id) {
                            	   override = false;  
                            	}
                            	});

                            	// create alert dialog
                            	AlertDialog alertDialog = alertDialogBuilder.create();

                            	// show it
                            	alertDialog.show();
                		}
                		else{
                			new efsbackup().execute();
                		}
                	}
                	})
                	.setNegativeButton("Restore",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {

                  		 new efsrestore().execute();
                			
                	}
                	});

                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();

                	// show it
                	alertDialog.show();
            		
                   
                }
            }
        });
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
		case DIALOG_INSTALL_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Installing..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
		case DIALOG_BACKUP_PROGRESS:
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Backing Up..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            return mProgressDialog;
		case DIALOG_RESTORE_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Restoring..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            return mProgressDialog;
		default:
                return null;
        }
    }
    class DownloadFileAsync extends AsyncTask<String, String, String> {
       
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream("/sdcard/freegee.tar");

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;

        }
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_DOWNLOAD_PROGRESS);
        	if(restoring){
        		new restore().execute();
        	}
        	else{
        		new unlock().execute();
        	}
        }
    }
    
    private class unlock extends AsyncTask<String, Integer, String>{

    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            removeDialog(DIALOG_DOWNLOAD_PROGRESS);
            showDialog(DIALOG_INSTALL_PROGRESS);
        }
    	int err = 0;
		protected String doInBackground(String...Params) {
    	//int err = 0;
    	String command = "busybox mv /sdcard/freegee.tar /data/local/tmp/ && cd /data/local/tmp/ && busybox tar xvf freegee.tar";
    	try {
			 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			//RootTools.getShell(true).add(command).waitForFinish();
 catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (err == 0){
    		publishProgress(0);
    		command = "cd /data/local/tmp/ && chmod 777 freegee-backup.sh && sh freegee-backup.sh";
    		try {
   			 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
   			//RootTools.getShell(true).add(command).waitForFinish();
    catch (InterruptedException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
    		if (err == 0){
    			publishProgress(1);
    			command = "cd /data/local/tmp/ && chmod 777 freegee-install.sh && sh freegee-install.sh";
    			try {
    				 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    				//RootTools.getShell(true).add(command).waitForFinish();
    	 catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            	
    			if (err !=0){
    				
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Error!");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("There was a problem installing. Attempting to reinstall backups.")
                	.setCancelable(false)
                	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		 new restore().execute();
                	}
                	});
                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();
                	// show it
                	alertDialog.show();
                	
    			}
    		   }
    		
    		else{
    			err=-1;
    			
    		    }
    		}
    	else {
    		err = -2;
    		
    		
    	}
    	
		return null;
		}
		
		protected void onProgressUpdate(String... progress) {

            if(progress[0] == "0"){
             mProgressDialog.setMessage("Creating Backups...");
             }
            if(progress[0] == "1"){
                mProgressDialog.setMessage("Installing...");
                }
       }

       @Override
       protected void onPostExecute(String unused) {
           dismissDialog(DIALOG_INSTALL_PROGRESS);
           if(err==-1){
        	   alertbuilder("Error!","There was a problem creating backups. Aborting.","Ok",0);
           }
           else if(err==-2){
        	   alertbuilder("Error!","There was a problem untaring the file. Please try again","Ok!",0);
           }
           else{
           alertbuilder("Success!","Success. Your "+varient+" Optimus G has been liberated!","Yay!",0);
           }
           
       }
    	
    }	
    
    class restore extends AsyncTask<String, String, String> {
    	int err = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_RESTORE_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
        	String command;
        	File freegeef=new File("/sdcard/freegee");
			  if(!freegeef.exists()){
				  freegeef.mkdirs();
			  }
        	File boot=new File("/sdcard/freegee/boot-backup.img");
        	File recovery=new File("/sdcard/freegee/recovery-backup.img");
        	File aboot=new File("/sdcard/freegee/aboot-backup.img");
 			  if(boot.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 		        	command = "dd if=/sdcard/freegee/boot-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			  }
 			  else{
 				  err=-1;
 			  }
 			  
 			  if(aboot.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	command = "dd if=/sdcard/freegee/aboot-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
 			  else{
 				  err=-2;
 			  }
 			  
 			  if(recovery.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	command = "dd if=/sdcard/freegee/recovery-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
 			  else {
 				  err = -3;
 			  }
			return null;
        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_RESTORE_PROGRESS);
        	if(err==0){
        	    alertbuilder("Success!","Success. Your Optimus G been restored up!","Yay!",0);
        	}
        	else if(err<=-1){
        		alertbuilder("Error!","Could not restore, backups not found! Do not reboot!","Boo!",0);
        	}
        	else{
        		alertbuilder("Error!","There was an error restoring your backups Do not reboot!","Boo!",0);
        	}
        	
        }
    }
    
    class efsbackup extends AsyncTask<String, String, String> {
    	int err = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_BACKUP_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
        	String command;
        	File freegeef=new File("/sdcard/freegee");
			  if(!freegeef.exists()){
				  freegeef.mkdirs();
			  }
              File efs1=new File("/sdcard/freegee/m9kefs1.img");
 			  if(!efs1.exists() || override == true){
 		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			  }
 	          File efs2=new File("/sdcard/freegee/m9kefs2.img");
 			  if(!efs2.exists() || override == true){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
 			  File efs3=new File("/sdcard/freegee/m9kefs3.img");
 			  if(!efs3.exists() || override == true){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
			return null;
        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_BACKUP_PROGRESS);
        	
        	 alertbuilder("Success!","Success. Your Optimus G EFS been backed up!","Yay!",0);
        	
        	
        }
    }
    
    class efsrestore extends AsyncTask<String, String, String> {
    	int err = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_RESTORE_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
        	String command;
        	File freegeef=new File("/sdcard/freegee");
			  if(!freegeef.exists()){
				  freegeef.mkdirs();
			  }
        	File efs1=new File("/sdcard/freegee/m9kefs1.img");
        	File efs2=new File("/sdcard/freegee/m9kefs3.img");
        	File efs3=new File("/sdcard/freegee/m9kefs2.img");
 			  if(efs1.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 		        	command = "dd if=/sdcard/freegee/m9kefs1.img of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			  }
 			  else{
 				  err=-1;
 			  }
 			  
 			  if(efs2.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	command = "dd if=/sdcard/freegee/m9kefs2.img of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
 			  else{
 				  err=-2;
 			  }
 			  
 			  if(efs3.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	command = "dd if=/sdcard/freegee/m9kefs3.img of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
 			  else {
 				  err = -3;
 			  }
			return null;
        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_RESTORE_PROGRESS);
        	if(err==0){
        	    alertbuilder("Success!","Success. Your Optimus G EFS been restored up!","Yay!",0);
        	}
        	else if(err<=-1){
        		alertbuilder("Error!","Could not restore, EFS backups not found!","Boo!",0);
        	}
        	else{
        		alertbuilder("Error!","There was an error restoring your EFS backups","Boo!",0);
        	}
        	
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
}