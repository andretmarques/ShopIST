package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;


import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Animation fromBottom;
    private Animation toBottom;
    private Animation rotateOpen;
    private Animation rotateClose;
    private boolean clicked = false;
    ExtendedFloatingActionButton joinButton;
    ExtendedFloatingActionButton createButton;
    FloatingActionButton addButton;

    protected LocationManager locationManager;
    GPSUpdater mGPS;
    TextView GPStext;
    TextView locationPicker;
    double lat = 0;
    double lon = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(),getString(R.string.key_google_apis_android));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        addButton = (FloatingActionButton) findViewById(R.id.add_btn);
        joinButton = (ExtendedFloatingActionButton) findViewById(R.id.join_btn);
        createButton = (ExtendedFloatingActionButton) findViewById(R.id.create_btn);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);


        mGPS = new GPSUpdater(this.getApplicationContext());

        GPStext = findViewById(R.id.GPSRoad);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationListenerGPS);
        }
    }

    LocationListener LocationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("OLA", "onLocationChanged: " + location);
            String address = getRoad(location.getLatitude(), location.getLongitude());
            GPStext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
            GPStext.setText(address);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude", "disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude", "enable");
        }
    };

    public void showCreatePopUp(View v) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.new_list_layout, (LinearLayout) findViewById(R.id.newListContainer));
        bottomSheetView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();



        bottomSheetView.findViewById(R.id.pick_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePicker();
                locationPicker = bottomSheetView.findViewById(R.id.list_location);
                if (updateTextView() != null) {
                    locationPicker.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
                    locationPicker.setText(updateTextView());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // User chose the "Settings" item, show the app settings UI...
                Log.d("TAG", "onOptionsItemSelected: Settings");
                return true;

            case R.id.rate:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Log.d("TAG", "onOptionsItemSelected: Rate");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void showPlacePicker() {

        Intent intent = new PlacePicker.IntentBuilder()
                .setLatLong(40.748672, -73.985628)  // Initial Latitude and Longitude the Map will load into
                .showLatLong(true)  // Show Coordinates in the Activity
                .setMapZoom(12.0f)  // Map Zoom Level. Default: 14.0
                .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                .setMarkerImageImageColor(R.color.colorPrimary)
                .setMapType(MapType.NORMAL)
                .setPlaceSearchBar(true, getString(R.string.key_google_apis_android)) //Activate GooglePlace Search Bar. Default is false/not activated. SearchBar is a chargeable feature by Google
                .onlyCoordinates(true)  //Get only Coordinates from Place Picker
                .hideLocationButton(true)   //Hide Location Button (Default: false)
                .disableMarkerAnimation(true)   //Disable Marker Animation (Default: false)
                .build(MainActivity.this);
        try {
            startActivityForResult(intent, 100);

        } catch (Exception ex) {
            Log.d("FAIL_BRO", "showPlacePicker: RIP");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100) {
            try {
                AddressData addressData = data.getParcelableExtra("ADDRESS_INTENT");
                lat = addressData.getLatitude();
                lon = addressData.getLongitude();
                Log.d("DDDDDDDDDOOOOONNNNEEEE", "onActivityResult: " + addressData.toString());
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String updateTextView() {
        if (lat != 0 && lon != 0) {
            return getRoad(lat, lon);
        } else return null;
    }


    public void onClickButton(View v) {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    public void setVisibility(Boolean clicked){
        if(!clicked) {
            joinButton.setClickable(true);
            joinButton.setVisibility(View.VISIBLE);
            createButton.setVisibility(View.VISIBLE);
        }
        else{
            createButton.setClickable(false);
            joinButton.setVisibility(View.GONE);
            createButton.setVisibility(View.GONE);

        }
    }

    public void setAnimation(Boolean clicked){
        if(!clicked){
            addButton.startAnimation(rotateOpen);
            joinButton.startAnimation(fromBottom);
            createButton.startAnimation(fromBottom);
        }
        else {
            joinButton.startAnimation(toBottom);
            createButton.startAnimation(toBottom);
            addButton.startAnimation(rotateClose);
        }
    }

    public String getRoad(double latitude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude,1);
            address = addresses.get(0).getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}