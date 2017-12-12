package com.example.averygrimes.phone_wallet_keys;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class SwipeRecyclerViewAdapter extends RecyclerSwipeAdapter<SwipeRecyclerViewAdapter.SimpleViewHolder>
{
    
    private Context mContext;
    private ArrayList<DeviceModel> deviceList;
    
    public SwipeRecyclerViewAdapter(Context context, ArrayList<DeviceModel> objects)
    {
        this.mContext = context;
        this.deviceList = objects;
    }
    
    
    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_layout, parent, false);
        return new SimpleViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        final DeviceModel item = deviceList.get(position);
        
        viewHolder.tvName.setText(item.getName());
        viewHolder.tvStatus.setText(item.getStatus());
        
        
        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        
        //drag from left
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper1));
        
        //drag from right
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wraper));
        
        
        //handling different event when swiping
        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }
            
            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }
            
            @Override
            public void onStartClose(SwipeLayout layout) {
                
            }
            
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }
            
            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }
            
            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });
        
        viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(mContext, item.getName() + " is " + item.getStatus(), Toast.LENGTH_SHORT).show();
            }
        });

        //Settings
        viewHolder.btn_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                Intent startIntent = new Intent(mContext, DeviceSettings.class);
                Bundle extrasForDeviceSettings = new Bundle();

                for(BluetoothDevice bt : pairedDevices)
                {
                    try {
                        Method method = bt.getClass().getMethod("getAliasName");
                        if(method != null)
                        {
                            if(((String)method.invoke(bt)).equals(viewHolder.tvName.getText().toString()))
                            {
                                extrasForDeviceSettings.putString("deviceAddress", bt.getAddress());
                                startIntent.putExtras(extrasForDeviceSettings);
                                mContext.startActivity(startIntent);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        viewHolder.tvSnooze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;

                final DeviceSettings ds = new DeviceSettings();
                ds.context = mContext;

                if(viewHolder.tvSnooze.getText().toString().equals("Snooze"))
                {
                    if(viewHolder.tvStatus.getText().toString().equals("Connected"))
                    {
                        mTimePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker timePicker, final int selectedHour, final int selectedMinute)
                            {
                                viewHolder.tvStatus.setText("Snoozed until " + selectedHour + ":" + selectedMinute);
                                viewHolder.tvSnooze.setText("Unsnooze");

                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                                for(BluetoothDevice bt : pairedDevices)
                                {
                                    try {
                                        Method method = bt.getClass().getMethod("getAliasName");
                                        if(method != null)
                                        {
                                            if(((String)method.invoke(bt)).equals(viewHolder.tvName.getText().toString()))
                                            {
                                                //when device is added will show up on the database table
                                                String Dname = bt.getName().toString();
                                                //String Dstatus = "";
                                                Date d=new Date();
                                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                                String currentDateTimeString = sdf.format(d);
                                                String Dtime = currentDateTimeString;

                                                Calendar c = Calendar.getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                                String formattedDate = df.format(c.getTime());
                                                String Ddate = formattedDate;

                                                ds.AddData(Dname,Dtime,Ddate,"Snoozed");
                                            }
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }



                                String temp = "";

                                try
                                {
                                    File file = mContext.getFileStreamPath("ThreadID.txt");

                                    if (!file.exists())
                                    {
                                        file.createNewFile();
                                    }

                                    FileInputStream reader = mContext.openFileInput(file.getName());

                                    byte[] input = new byte[reader.available()];
                                    while (reader.read(input) != -1) {}
                                    temp += new String(input);
                                }
                                catch (IOException e) {
                                    Log.e("Exception", "File Read failed: " + e.toString());
                                }

                                long threadID = Long.parseLong(temp);

                                for (Thread t : Thread.getAllStackTraces().keySet())
                                    if (t.getId()==threadID)
                                        t.interrupt();

                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        while(true)
                                        {
                                            String yourTime = selectedHour + ":" + selectedMinute + ":00";

                                            //get your today date as string
                                            String today = (String) DateFormat.format(
                                                    "hh:mm:ss", new Date());

                                            //Convert the two time string to date formate
                                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                                            Date date1;
                                            Date date2;

                                            try
                                            {
                                                date1 = sdf.parse(yourTime);
                                                date2 = sdf.parse(today);

                                                //do the comparison
                                                if (!date1.after(date2))
                                                {
                                                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                                                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                                                    for(BluetoothDevice bt : pairedDevices)
                                                    {
                                                        try {
                                                            Method method = bt.getClass().getMethod("getAliasName");
                                                            if(method != null)
                                                            {
                                                                if(((String)method.invoke(bt)).equals(viewHolder.tvName.getText().toString()))
                                                                {
                                                                    viewHolder.tvStatus.setText("Connected");
                                                                    viewHolder.tvSnooze.setText("Snooze");
                                                                    ds.deviceAddress = bt.getAddress();
                                                                    ds.context = mContext;
                                                                    Thread myThread = new Thread(ds.connectToDevice);
                                                                    myThread.start();

                                                                    try
                                                                    {
                                                                        File file = mContext.getFileStreamPath("ThreadID.txt");

                                                                        file.createNewFile();


                                                                        FileOutputStream writer = mContext.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                                                                        byte[] bytesArray = Long.toString(myThread.getId()).getBytes();

                                                                        writer.write(bytesArray);

                                                                        writer.close();
                                                                    }
                                                                    catch (IOException e) {
                                                                        Log.e("Exception", "File write failed: " + e.toString());
                                                                    }

                                                                    //when device is added will show up on the database table
                                                                    String Dname = bt.getName().toString();
                                                                    //String Dstatus = "";
                                                                    Date d=new Date();
                                                                    sdf = new SimpleDateFormat("hh:mm a");
                                                                    String currentDateTimeString = sdf.format(d);
                                                                    String Dtime = currentDateTimeString;

                                                                    Calendar c = Calendar.getInstance();
                                                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                                                    String formattedDate = df.format(c.getTime());
                                                                    String Ddate = formattedDate;

                                                                    ds.AddData(Dname,Dtime,Ddate,"Unsnoozed");
                                                                    return;
                                                                }
                                                            }
                                                        } catch (NoSuchMethodException e) {
                                                            e.printStackTrace();
                                                        } catch (InvocationTargetException e) {
                                                            e.printStackTrace();
                                                        } catch (IllegalAccessException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                                else
                                                {

                                                }
                                            }
                                            catch(Exception ex)
                                            {
                                                Log.d("SwipeRecyclerView", "Error: " + ex);
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }, hour, minute, false);//Yes 24 hour time
                        mTimePicker.setTitle("Select Time");
                        mTimePicker.show();
                    }
                    else
                    {
                        Toast.makeText(mContext, "Device is disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(viewHolder.tvSnooze.getText().toString().equals("Unsnooze"))
                {
                    viewHolder.tvStatus.setText("Connected");
                    viewHolder.tvSnooze.setText("Snooze");

                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    for(BluetoothDevice bt : pairedDevices)
                    {
                        try {
                            Method method = bt.getClass().getMethod("getAliasName");
                            if(method != null)
                            {
                                if(((String)method.invoke(bt)).equals(viewHolder.tvName.getText().toString()))
                                {
                                    //when device is added will show up on the database table
                                    String Dname = bt.getName().toString();
                                    Date d=new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                    String currentDateTimeString = sdf.format(d);
                                    String Dtime = currentDateTimeString;

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                    String formattedDate = df.format(c.getTime());
                                    String Ddate = formattedDate;

                                    ds.AddData(Dname,Dtime,Ddate,"Snoozed");

                                    ds.deviceAddress = bt.getAddress();
                                    Thread myThread = new Thread(ds.connectToDevice);
                                    myThread.start();

                                    try
                                    {
                                        File file = mContext.getFileStreamPath("ThreadID.txt");

                                        file.createNewFile();


                                        FileOutputStream writer = mContext.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                                        byte[] bytesArray = Long.toString(myThread.getId()).getBytes();

                                        writer.write(bytesArray);

                                        writer.close();
                                    }
                                    catch (IOException e) {
                                        Log.e("Exception", "File write failed: " + e.toString());
                                    }
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        
        mItemManger.bindView(viewHolder.itemView, position);
    }
    
    @Override
    public int getItemCount()
    {
        return deviceList.size();
    }
    
    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
    
    public static class SimpleViewHolder extends RecyclerView.ViewHolder{
        public SwipeLayout swipeLayout;
        public TextView tvName;
        public TextView tvStatus;
        public TextView tvSnooze;
        public ImageButton btn_Settings;
        public SimpleViewHolder(View itemView)
        {
            super(itemView);
            
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            tvSnooze = (TextView) itemView.findViewById(R.id.tvSnooze);
            btn_Settings = (ImageButton) itemView.findViewById(R.id.btn_Settings);
        }
    }
}
