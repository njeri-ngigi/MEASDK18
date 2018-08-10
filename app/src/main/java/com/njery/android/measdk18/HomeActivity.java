package com.njery.android.measdk18;

import android.Manifest;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.njery.android.measdk18.data.MeaContract;
import com.njery.android.measdk18.data.MeaContract.ContactsEntry;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        RecognitionListener{

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CALL_PERMISSION = 2;
    private static final int REQUEST_SMS_PERMISSION = 3;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 4;

    private static final int EXISTING_PET_LOADER = 0;

    private Button mButtonCallOperator, mButtonSendSos;
    private TextView mFullNameTV;

    private FusedLocationProviderClient mFusedLocationClient;

    String mEmergencyText;

    private SpeechRecognizer mRecognizer;
    private static final String MENU_SEARCH = "menu";

    public  void createNotification(){
        Intent homeIntent = new Intent(this, HomeActivity.class);

        if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        }

        String number = "+254702402781";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel: " + number));

        PendingIntent pendingCallIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), callIntent, 0);
        PendingIntent pendingSOSIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), homeIntent, 0);
        PendingIntent pendingHomeIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), homeIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("M.E.A")
                .setContentText("I'm always here ...")
                .setSmallIcon(R.drawable.ic_mea_white)
                .addAction(R.drawable.ic_call, "", pendingCallIntent)
                .addAction(R.drawable.ic_home, "", pendingHomeIntent)
                .addAction(R.drawable.ic_sms, "", pendingSOSIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(0, notification);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userPreferences = MeaSharedPreferences.getStoredQuery(this);

        if (userPreferences == null){
            Intent intent = new Intent(HomeActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_home);
            createNotification();

            setTitle(R.string.app_name);

            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);

            ActionBar actionBar = this.getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(false);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // check permissions
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getApplicationContext(),
                    Manifest.permission.RECORD_AUDIO);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
                return;
            }

            // Initializing recognizer is time-consuming and involves IO
            // Execute it in async task
            new SetupSpeechTask(this).execute();

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mEmergencyText = MeaSharedPreferences.getPrefUserSos(HomeActivity.this);

            mFullNameTV = findViewById(R.id.tv_full_name);

            mButtonCallOperator = findViewById(R.id.button_call_operator);
            mButtonSendSos = findViewById(R.id.button_send_sos);

            mButtonCallOperator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    call("0702402781");
                }
            });

            mButtonSendSos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Snackbar.make(v, mEmergencyText, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    getLocation();
                }
            });

            FloatingActionButton fab = findViewById(R.id.fab_call);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View view) {
                    Snackbar.make(view, "Calling ...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        String username = MeaSharedPreferences.getPrefUserName(this);
        mFullNameTV.setText(username);

    }

    private void call(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel: " + number));

        if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        }
        startActivity(callIntent);
    }

    private void sendSOS(String message){
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class), 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("0702402781", "0773942978", message, pendingIntent, null);
    }

    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    new FetchAddressTask(HomeActivity.this).execute(location);
                }
                else{
                    Toast.makeText(HomeActivity.this, "Location unavailable", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class FetchAddressTask extends AsyncTask<Location, Void, String>{
        private Context mContext;

        FetchAddressTask(Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            Location location = locations[0];
            List<Address> addresses = null;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String resultMessage = ""; // = "\n Longitude: \t" + longitude + "\n" + "\t Latitude: " + latitude

            try{
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                if(addresses == null || addresses.size() == 0) {
                    if (resultMessage.isEmpty())
                        resultMessage = "Address not found";
                }
                else{
                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                        addressParts.add(address.getAddressLine(i));
                    }
                    resultMessage = TextUtils.join("\n", addressParts);
                }
            }catch (IOException ioException){
                resultMessage = "Service not available" + "\n" + "Longitude: \t" + longitude + "\n" + "Latitude: \t" + latitude;
            }catch (IllegalArgumentException i){
                resultMessage = "Invalid coordinates provided";
            }
            return resultMessage;
        }

        @Override
        protected void onPostExecute(String s) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a dd-MMMM-yyyy");
            String currentTime = dateFormat.format(calendar.getTime());
            String emergencyText = mEmergencyText + "\n" + "My Location: \t" + s + "\n" + "Time: \t" + currentTime;

            mFullNameTV.setText(emergencyText);
            sendSOS(emergencyText);
            super.onPostExecute(s);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_contacts){
            intent = new Intent(HomeActivity.this, ContactsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_profile) {
            intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contacts ) {
            intent = new Intent(HomeActivity.this, ContactsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about_developer) {
            intent = new Intent(HomeActivity.this, DevelopersActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_rate_us) {
            Toast.makeText(HomeActivity.this, "Thanks for the 5 stars!",
                    Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MeaContract.ContactsEntry._ID,
                MeaContract.ContactsEntry.COLUMN_NAME,
                MeaContract.ContactsEntry.COLUMN_PHONE_NUMBER
        };

        CursorLoader cursorLoaderContacts = new CursorLoader(this,
                ContactsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        return cursorLoaderContacts;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class SetupSpeechTask extends AsyncTask<Void, Void, Exception>{
        WeakReference<HomeActivity> mActivityReference;

        SetupSpeechTask(HomeActivity activity){
            this.mActivityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try{
                Assets assets = new Assets(mActivityReference.get());
                File assetDir = assets.syncAssets();
                mActivityReference.get().setupRecognizer(assetDir);
            } catch (IOException e){
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e != null){
                Toast.makeText(mActivityReference.get(),
                        "Failed to initialize speech recognizer",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mActivityReference.get(),
                        "Use call, send, list, profile, contacts or back speech commands",
                        Toast.LENGTH_LONG).show();
                mActivityReference.get().reset();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                new SetupSpeechTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mRecognizer != null) {
            mRecognizer.cancel();
            mRecognizer.shutdown();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        reset();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis != null){
            String text = hypothesis.getHypstr().trim();

            if(text.equals("call")){
                String number = "+254702402781";
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel: " + number));

                if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PERMISSION);
                }
                startActivity(callIntent);
            }
            if(text.equals("send")){
                getLocation();
            }
            if(text.equals("list")){

            }

            if(text.equals("profile")){
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
            if(text.equals("contacts")){
                Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
            if(text.equals("back")){
                Context context = HomeActivity.this;
                if(getApplicationContext() == context){
                    return;
                } else{
                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
            if(text.equals("exit")){
                finish();
            }

            Toast.makeText(getApplicationContext(),
                    text, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onError(Exception e) {
        String error = e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onTimeout() {
        reset();
    }

    private void reset(){
        mRecognizer.stop();
        mRecognizer.startListening(MENU_SEARCH);
    }

    private void setupRecognizer(File assetsDir) throws IOException{
        mRecognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space
                .setKeywordThreshold(1e-20f)
                .getRecognizer();

        mRecognizer.addListener(this);

        // Create grammar search for digit recognition
        File menuGrammar = new File(assetsDir, "mea.gram");
        mRecognizer.addKeywordSearch(MENU_SEARCH, menuGrammar);

    }
}
