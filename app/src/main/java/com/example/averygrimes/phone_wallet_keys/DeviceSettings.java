package com.example.averygrimes.phone_wallet_keys;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import android.app.AlertDialog;
import android.widget.Switch;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;


public class DeviceSettings extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "DeviceSettings";
    Button btn_DeviceSettings_Delete, btn_DeviceSettings_EditName, btn_DeviceSettings_Notification;

    BluetoothAdapter bluetoothAdapter;
    String deviceAddress;
    Thread myThread;
    long threadID;
    String connectedDeviceAddress;

    // Used to connect to the bluetooth device
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Uri uriSound;

    //Access the database
    Database myDb;

    // Used for sending notification to phone
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        context = this;

        Bundle extrasForDeviceSettings = getIntent().getExtras();

        deviceAddress = extrasForDeviceSettings.getString("deviceAddress");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        getSupportActionBar().setTitle(bluetoothDevice.getName());

        btn_DeviceSettings_Delete = (Button) findViewById(R.id.btn_DeviceSettings_Delete);
        btn_DeviceSettings_EditName = (Button) findViewById(R.id.btn_DeviceSettings_EditName);
        btn_DeviceSettings_Notification = (Button) findViewById(R.id.btn_DeviceSettings_Notification);

        btn_DeviceSettings_Delete.setOnClickListener(this);
        btn_DeviceSettings_EditName.setOnClickListener(this);
        btn_DeviceSettings_Notification.setOnClickListener(this);
    }

    public Runnable connectToDevice = new Runnable()
    {
        @Override
        public void run()
        {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

            while (true)
            {
                Log.d(TAG, bluetoothDevice.getName());
                BluetoothSocket socket = null;

                try {
                    socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                } catch (IOException e1) {
                    Log.d(TAG, "socket not created");
                    e1.printStackTrace();
                }

                try {
                    socket.connect();
                    Log.e("", "Connected");
                } catch (IOException e) {
                    Log.e("", e.getMessage());

                    try {
                        Log.e("", "trying fallback...");

                        socket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bluetoothDevice, 1);
                        socket.connect();
                        Log.e("", "Connected");
                    } catch (Exception e2)
                    {

                        //when device is added will show up on the database table
                        String Dname = bluetoothDevice.getName().toString();
                        //String Dstatus = "";
                        Date d=new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        String currentDateTimeString = sdf.format(d);
                        String Dtime = currentDateTimeString;

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                        String formattedDate = df.format(c.getTime());
                        String Ddate = formattedDate;

                        AddData(Dname,Dtime,Ddate,"Lost");

                        createNotification(Dname, Dtime);

                        try
                        {
                            File file = context.getFileStreamPath("ConnectedDevice.txt");

                            file.createNewFile();

                            String temp = "Empty";

                            FileOutputStream writer = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                            byte[] bytesArray = temp.getBytes();

                            writer.write(bytesArray);

                            writer.close();

                            FileInputStream reader = context.openFileInput(file.getName());

                            String content = "";

                            byte[] input = new byte[reader.available()];
                            while (reader.read(input) != -1) {}
                            content += new String(input);

                            Log.d("myTag",content);
                        }
                        catch (IOException ex) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }

                        Log.d("", "Couldn't establish Bluetooth connection!: " + e2);
                        return;
                    }
                }

                try {
                    socket.close();
                } catch (Exception ex) {

                }

                try {
                    Thread.sleep(5000);
                } catch (Exception ex)
                {
                    return;
                }
            }
        }

    };

    //The menu bar will show on the top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu);

        MenuItem itemSwitch = menu.findItem(R.id.myswitch);
        itemSwitch.setActionView(R.layout.use_switch);

        final Switch sw = (Switch) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.action_switch);

        String temp = "";

        try
        {
            File file = context.getFileStreamPath("ConnectedDevice.txt");
            FileInputStream reader = openFileInput(file.getName());

            byte[] input = new byte[reader.available()];
            while (reader.read(input) != -1) {}
            temp += new String(input);
        }
        catch (IOException e) {
            Log.e("Exception", "File Read failed: " + e.toString());
        }

        if(temp.equals(deviceAddress))
        {
            sw.setEnabled(true);
            sw.toggle();
        }
        else if(!temp.equals("Empty"))
        {
            sw.setEnabled(false);
        }
        else
        {
            sw.setEnabled(true);
        }

        final String content = temp;

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

                if(bluetoothAdapter.isEnabled())
                {
                    if(isChecked && content.equals("Empty"))
                    {
                        myThread = new Thread(connectToDevice);
                        myThread.start();
                        threadID = myThread.getId();


                        try
                        {
                            File file = getFileStreamPath("ThreadID.txt");

                            file.createNewFile();


                            FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
                            byte[] bytesArray = Long.toString(threadID).getBytes();

                            writer.write(bytesArray);

                            writer.close();

                            FileInputStream reader = openFileInput(file.getName());

                            String content = "";

                            byte[] input = new byte[reader.available()];
                            while (reader.read(input) != -1) {}
                            content += new String(input);

                            Log.d("myTag",content);
                        }
                        catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }


                        connectedDeviceAddress = deviceAddress;

                        //when device is added will show up on the database table
                        String Dname = bluetoothDevice.getName().toString();
                        //String Dstatus = "";
                        Date d=new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        String currentDateTimeString = sdf.format(d);
                        String Dtime = currentDateTimeString;

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                        String formattedDate = df.format(c.getTime());
                        String Ddate = formattedDate;

                        AddData(Dname,Dtime,Ddate,"Connected");

                        try
                        {
                            File file = getFileStreamPath("ConnectedDevice.txt");

                            file.createNewFile();

                            FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
                            byte[] bytesArray = deviceAddress.getBytes();

                            writer.write(bytesArray);

                            writer.close();

                            FileInputStream reader = openFileInput(file.getName());

                            String content = "";

                            byte[] input = new byte[reader.available()];
                            while (reader.read(input) != -1) {}
                            content += new String(input);

                            Log.d("myTag",content);
                        }
                        catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                    }
                    else if(isChecked && !content.equals("Empty"))
                    {
                        Toast.makeText(getApplicationContext(), "Another device is currently connected", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        //when device is added will show up on the database table
                        String Dname = bluetoothDevice.getName().toString();
                        //String Dstatus = "";
                        Date d=new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        String currentDateTimeString = sdf.format(d);
                        String Dtime = currentDateTimeString;

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                        String formattedDate = df.format(c.getTime());
                        String Ddate = formattedDate;

                        AddData(Dname,Dtime,Ddate,"Disconnected");


                        String temp = "";

                        try
                        {
                            File file = getFileStreamPath("ThreadID.txt");
                            FileInputStream reader = openFileInput(file.getName());

                            byte[] input = new byte[reader.available()];
                            while (reader.read(input) != -1) {}
                            temp += new String(input);
                        }
                        catch (IOException e) {
                            Log.e("Exception", "File Read failed: " + e.toString());
                        }

                        threadID = Long.parseLong(temp);

                        for (Thread t : Thread.getAllStackTraces().keySet())
                            if (t.getId()==threadID)
                                t.interrupt();
                        //myThread.interrupt();
                        //connectedDeviceAddress = null;
                    }
                }
                else
                {
                    try
                    {
                        File file = getFileStreamPath("ConnectedDevice.txt");

                        file.createNewFile();

                        String c = "Empty";

                        FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
                        byte[] bytesArray = c.getBytes();

                        writer.write(bytesArray);

                        writer.close();

                        FileInputStream reader = openFileInput(file.getName());

                        String content = "";

                        byte[] input = new byte[reader.available()];
                        while (reader.read(input) != -1) {}
                        content += new String(input);

                        Log.d("myTag",content);
                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                }
            }
            //}
        });
        return true;
    }

    //used to generate notification
    public void createNotification(String deviceName, String time)
    {
        String content = "";
        File file = context.getFileStreamPath("NotificationSound.txt");
        String[] notificationList = new String[1];

        try
        {
            if (!file.exists())
            {
                uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            else
            {
                FileInputStream reader = context.openFileInput(file.getName());

                byte[] input = new byte[reader.available()];
                while (reader.read(input) != -1) {}

                content += new String(input);

                notificationList = content.split(",");

                String[] temp;
                for(int i = 0; i < notificationList.length; i++)
                {
                    temp = notificationList[i].split("\\|");

                    if(temp[0].equals(deviceAddress))
                    {
                        uriSound = Uri.parse(temp[1]);
                    }
                }
            }

        }
        catch (IOException e)
        {
            Log.e("Exception", "File Read failed: " + e.toString());
        }

        Intent intent = new Intent(context,DeviceSettings.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent, 0);

        //Build the large notification
        Drawable drawable= ContextCompat.getDrawable(context,R.drawable.logoteam);

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        notification = new NotificationCompat.Builder(context);
        notification.setAutoCancel(true);
        notification.setSound(uriSound);

        //Build the notification
        notification.setSmallIcon(R.drawable.logoteam);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Lost Device");
        notification.setContentText(deviceName + " was lost at " + time);
        notification.setLargeIcon(bitmap);
        notification.setContentIntent(pendingIntent);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        notification.setVibrate(pattern);

        //Build notification and issues it
        NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }

    @Override
    public void onClick(View view)
    {
        final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        switch (view.getId())
        {
            case R.id.btn_DeviceSettings_Delete:
            {
                AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
                a_builder.setMessage("Are you sure you want to delete this device?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                try
                                {
                                    Method method = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                                    method.invoke(bluetoothDevice, (Object[]) null);

                                    //when device is added will show up on the database table
                                    String Dname = bluetoothDevice.getName().toString();
                                    //String Dstatus = "";
                                    Date d=new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                    String currentDateTimeString = sdf.format(d);
                                    String Dtime = currentDateTimeString;

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                    String formattedDate = df.format(c.getTime());
                                    String Ddate = formattedDate;

                                    AddData(Dname,Dtime,Ddate,"Deleted");

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                dialog.cancel();
                            }
                        });
                AlertDialog alert = a_builder.create();
                alert.show();

                break;
            }
            case R.id.btn_DeviceSettings_EditName:
            {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(DeviceSettings.this);
                View promptsView = li.inflate(R.layout.input_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        DeviceSettings.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id)
                                    {
                                        String result = userInput.getText().toString();


                                        try {
                                            Method method = bluetoothDevice.getClass().getMethod("setAlias", String.class);
                                            if(method != null) {
                                                method.invoke(bluetoothDevice, result);
                                            }
                                        } catch (NoSuchMethodException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                break;
            }
            case R.id.btn_DeviceSettings_Notification:
            {
                Context context = this.getApplicationContext();
                Intent startIntent = new Intent(getApplicationContext(), NotificationsList.class);
                startIntent.putExtra("deviceAddress", deviceAddress);
                context.startActivity(startIntent);
            }
        }
    }

    public void AddData(String bName, String bTime, String bDate, String bStatus)
    {
        //access database class
        myDb = new Database(context);

        boolean insertData = myDb.addData(bName,bTime,bDate, bStatus);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}