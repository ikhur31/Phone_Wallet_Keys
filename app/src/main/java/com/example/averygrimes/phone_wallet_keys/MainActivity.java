package com.example.averygrimes.phone_wallet_keys;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import java.io.FileOutputStream;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import java.util.Date;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import com.daimajia.swipe.util.Attributes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.lang.String;
import java.util.UUID;
import android.widget.LinearLayout;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.bluetooth.BluetoothDevice;
import android.widget.ListView;
import android.app.Dialog;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.DialogInterface.OnDismissListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "MainActivity"; // Used for debugging

    // Used for sending notification to phone
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;

    // Used to connect to the bluetooth device
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Empty References
    private Button btn_AddDevice, btn_BluetoothSwitch, btn_Scan, btn_MakeDiscoverable;
    BluetoothAdapter bluetoothAdapter; // Used for Bluetooth functions

    // Empty References for paired bluetooth devices list
    private TextView tvEmptyTextView;
    private RecyclerView mRecyclerView;
    private ArrayList<DeviceModel> pairedDeviceList;
    SwipeRecyclerViewAdapter SwipeAdapter;

    // Empty References for scanned bluetooth devices list
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView bluetooth_ScanList;
    Dialog dialog;

    //Empty References for changing color
    LinearLayout linearLayout;
    ConstraintLayout constraintLayout;
    ActionBar actionBar;
    View view;
    int themeclick;

    //Access the database
    Database myDb;
    ProgressDialog mProgressDialog;

    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counter = 3;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try
        {
            File file = getFileStreamPath("ConnectedDevice.txt");
            file.createNewFile();

            String c = "Empty";

            FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            byte[] bytesArray = c.getBytes();

            writer.write(bytesArray);

            writer.close();

            Log.d("onCreate", "ConnectedDevice is Empty");
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        // Connect References
        btn_AddDevice = (Button) findViewById(R.id.Btn_AddDevice);
        btn_BluetoothSwitch = (Button) findViewById(R.id.Btn_BluetoothSwitch);
        tvEmptyTextView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        pairedDeviceList = new ArrayList<>();

        // If buttons are clicked, go to onclick method
        btn_AddDevice.setOnClickListener(this);
        btn_BluetoothSwitch.setOnClickListener(this);

        //access database class
        myDb = new Database(this);

        // Ask to turn on bluetooth if it is off at the start
        if(!bluetoothAdapter.isEnabled())
        {
            btn_AddDevice.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        else if(bluetoothAdapter.isEnabled())
        {
            btn_BluetoothSwitch.setText("Bluetooth Off");
        }

        setTheme();
    }

    //used to generate notification
    public void createNotification()
    {
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        //Build the notification
        notification.setSmallIcon(R.drawable.oreo);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Lost Device Title");
        notification.setContentText("Body of the notification");

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //Build notification and issues it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.Btn_AddDevice:
            {
                if(!bluetoothAdapter.isEnabled())
                {
                    Toast.makeText(getApplicationContext(), "Turn bluetooth on first!", Toast.LENGTH_LONG).show();
                }
                else if(bluetoothAdapter.isEnabled())
                {
                    dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.popup_window);
                    dialog.setTitle("Title...");
                    bluetooth_ScanList= (ListView) dialog.findViewById(R.id.Bluetooth_ScanList);
                    dialog.show();

                    dialog.setOnDismissListener(new OnDismissListener()
                    {

                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            mBTDevices = new ArrayList<>();
                        }
                    });

                    btn_Scan = (Button) dialog.findViewById(R.id.Btn_Scan);
                    btn_MakeDiscoverable = (Button) dialog.findViewById(R.id.Btn_MakeDiscoverable);

                    btn_Scan.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
                            mBTDevices = new ArrayList<>();

                            if(bluetoothAdapter.isDiscovering()){
                                bluetoothAdapter.cancelDiscovery();
                                Log.d(TAG, "btnDiscover: Canceling discovery.");

                                //check BT permissions in manifest
                                checkBTPermissions();

                                bluetoothAdapter.startDiscovery();
                                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                            }
                            else if(!bluetoothAdapter.isDiscovering()){

                                //check BT permissions in manifest
                                checkBTPermissions();

                                bluetoothAdapter.startDiscovery();
                                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                            }

                            bluetooth_ScanList.setOnItemClickListener(new OnItemClickListener()
                            {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                                {
                                    //first cancel discovery because its very memory intensive.
                                    bluetoothAdapter.cancelDiscovery();

                                    Log.d(TAG, "onItemClick: You Clicked on a device.");
                                    String deviceName = mBTDevices.get(i).getName();
                                    String deviceAddress = mBTDevices.get(i).getAddress();

                                    Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                                    Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                                    //when device is added will show up on the database table
                                    String Dname = deviceName.toString();
                                    //String Dstatus = "";
                                    Date d=new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                    String currentDateTimeString = sdf.format(d);
                                    String Dtime = currentDateTimeString;

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                    String formattedDate = df.format(c.getTime());
                                    String Ddate = formattedDate;

                                    AddData(Dname,Dtime,Ddate,"Paired");

                                    //Broadcasts when bond state changes (ie:pairing)
                                    IntentFilter filter = new IntentFilter();
                                    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                                    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                                    registerReceiver(mBroadcastReceiver4, filter);

                                    counter = 0;

                                    //create the bond.
                                    //NOTE: Requires API 17+? I think this is JellyBean
                                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
                                    {
                                        Log.d(TAG, "Trying to pair with " + deviceName);
                                        mBTDevices.get(i).createBond();
                                    }

                                    //initprogress dialog
                                    mProgressDialog = ProgressDialog.show(MainActivity.this,"Pairing Device"
                                            ,"Please Wait...",true);
                                }
                            });
                        }
                    });

                    btn_MakeDiscoverable.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            //Broadcasts when bond state changes (ie:pairing)
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                            registerReceiver(mBroadcastReceiver4, filter);

                            Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                            startActivity(discoverableIntent);

                            IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                            registerReceiver(mBroadcastReceiver2,intentFilter);
                        }
                    });

                    mRecyclerView.setAdapter(SwipeAdapter);
                }

                break;
            }
            case R.id.Btn_BluetoothSwitch:
            {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
                break;
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        btn_BluetoothSwitch.setText("Bluetooth On");
                        btn_AddDevice.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        Toast.makeText(getApplicationContext(), "Turning off Bluetooth", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        btn_BluetoothSwitch.setText("Bluetooth Off");
                        btn_AddDevice.getBackground().setColorFilter(null);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

                if(device.getName() != null)
                {
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    bluetooth_ScanList.setAdapter(mDeviceListAdapter);
                }
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

            if(device.getBondState() == device.BOND_BONDED)
            {
                Intent startIntent = new Intent(getBaseContext(), DeviceSettings.class);
                Bundle extrasForDeviceSettings = new Bundle();
                extrasForDeviceSettings.putString("deviceAddress", device.getAddress());
                startIntent.putExtras(extrasForDeviceSettings);
                getBaseContext().startActivity(startIntent);

                unregisterReceiver(this);
            }

        }
    };

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    public void enableDisableBT()
    {
        if(bluetoothAdapter == null)
        {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
            Toast.makeText(getApplicationContext(), "Something is wrong with the Bluetooth", Toast.LENGTH_LONG).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(bluetoothAdapter.isEnabled())
        {
            Log.d(TAG, "enableDisableBT: disabling BT.");
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    public void createConnectedList()
    {
        pairedDeviceList.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices(); // Get list of paired deviced

        String temp = "";

        try
        {
            File file = getFileStreamPath("ConnectedDevice.txt");
            FileInputStream reader = openFileInput(file.getName());

            byte[] input = new byte[reader.available()];
            while (reader.read(input) != -1) {}
            temp += new String(input);
        }
        catch (IOException e) {
            Log.e("Exception", "File Read failed: " + e.toString());
        }

        // Devices with edited names are known as Alias Names. This will display the Alias name.
        for(BluetoothDevice bt : pairedDevices)
        {
            try
            {
                Method method = bt.getClass().getMethod("getAliasName");

                if(method != null)
                {
                    if(temp.equals(bt.getAddress()))
                    {
                        pairedDeviceList.add(new DeviceModel((String)method.invoke(bt), "Connected"));
                    }
                    else
                    {
                        pairedDeviceList.add(new DeviceModel((String)method.invoke(bt), "Disconnected"));
                    }
                }
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));

        if(pairedDeviceList.isEmpty()){
            mRecyclerView.setVisibility(View.GONE);
            tvEmptyTextView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyTextView.setVisibility(View.GONE);
        }

        //creating adapter object
        SwipeAdapter = new SwipeRecyclerViewAdapter(MainActivity.this, pairedDeviceList);


        // Setting Mode to Single to reveal bottom View for one item in List
        // Setting Mode to Mutliple to reveal bottom Views for multile items in List
        ((SwipeRecyclerViewAdapter) SwipeAdapter).setMode(Attributes.Mode.Single);

        mRecyclerView.setAdapter(SwipeAdapter);

        /**Scroll listener**/
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
                Log.e("RecyclerView", "onScrollStateChanged");
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    //The menu bar will show on the top right
    @Override
    public boolean onCreateOptionsMenu(Menu dot){
        getMenuInflater().inflate(R.menu.main, dot);
        return true;
    }

    //Selection for the menu bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id =item.getItemId();

        if (id==R.id.id_theme){
            Intent intentsetting = new Intent(MainActivity.this, Themes.class);
            startActivity(intentsetting);
            return true;
        }
        if (id==R.id.id_help){
            /*Intent intentHelp = new Intent(MainActivity.this,Help.class);
            startActivity(intentHelp);*/
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://averyg21.github.io/PWKWebsite/")));
            return true;
        }
        if (id==R.id.id_history){
            Intent intenthistory = new Intent(MainActivity.this,History.class);
            startActivity(intenthistory);
            return true;
        }
        return true;
    }

    public void setTheme()
    {
        // Get which theme was chosen by User previously
        try
        {
            File file = getFileStreamPath("themes.txt");
            FileInputStream reader = openFileInput(file.getName());

            String content = "";

            byte[] input = new byte[reader.available()];
            while (reader.read(input) != -1) {}
            content += new String(input);
            themeclick = Integer.parseInt(content);
        }
        catch (IOException e) {
            Log.e("Exception", "File Read failed: " + e.toString());
        }

        //changes the color of background depending on theme class
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_main_layout);
        actionBar = getSupportActionBar();

        if (themeclick == 1)
        {
            linearLayout.setBackgroundResource(R.color.colordefault);
            constraintLayout.setBackgroundResource(R.color.colordefault2);
            actionBar.setBackgroundDrawable( // if it's ActionBar
                    new ColorDrawable(
                            actionBar.getThemedContext().getResources().getColor(R.color.colordefault2)));
        }
        if (themeclick == 2){
            linearLayout.setBackgroundResource(R.color.colorBlack);
            constraintLayout.setBackgroundResource(R.color.colorRed);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#800000")));
        }
        if (themeclick == 3){
            linearLayout.setBackgroundResource(R.color.colorTan);
            constraintLayout.setBackgroundResource(R.color.colorBeach1);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4da6ff")));
        }
        if (themeclick == 4){
            linearLayout.setBackgroundResource(R.color.colorFall1);
            constraintLayout.setBackgroundResource(R.color.colorFall2);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3d0099")));
        }
    }

    public void AddData(String bName, String bTime, String bDate, String bStatus)
    {
        boolean insertData = myDb.addData(bName,bTime,bDate, bStatus);

        if(insertData==true){
            Toast.makeText(getApplicationContext(),"Successfully Entered Data!",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"Something went wrong :(",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        try
        {
            unregisterReceiver(mBroadcastReceiver1);
        }
        catch (Exception ex)
        {

        }

        try
        {
            unregisterReceiver(mBroadcastReceiver2);
        }
        catch (Exception ex)
        {

        }

        try
        {
            unregisterReceiver(mBroadcastReceiver3);
        }
        catch (Exception ex)
        {

        }

        try
        {
            unregisterReceiver(mBroadcastReceiver4);
        }
        catch (Exception ex)
        {

        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setTheme();
        createConnectedList();

        if(mProgressDialog != null && counter == 1)
        {
            mProgressDialog.dismiss();
        }

        if(dialog != null && counter == 0)
        {
            dialog.dismiss();
        }

        counter++;
    }
}