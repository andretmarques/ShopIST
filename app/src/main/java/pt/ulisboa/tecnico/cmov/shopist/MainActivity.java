package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.schibstedspain.leku.LocationPickerActivity;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(),getString(R.string.key_google_apis_android));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
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
            }
        });
    }

    private void showPlacePicker() {

        Intent locationPickerIntent = new LocationPickerActivity.Builder()
                .withLocation(41.4036299, 2.1743558)
                .withGeolocApiKey(getString(R.string.key_google_apis_android))
                .withSearchZone("es_ES")
                .withDefaultLocaleSearchZone()
                .shouldReturnOkOnBackPressed()
                .withStreetHidden()
                .withCityHidden()
                .withZipCodeHidden()
                .withSatelliteViewHidden()
                .withGoogleTimeZoneEnabled()
                .withVoiceSearchHidden()
                .withUnnamedRoadHidden()
                .build(this.getApplicationContext());
        try {
            startActivityForResult(locationPickerIntent, 1);

        } catch (Exception ex) {
            Log.d("FAIL_BRO", "showPlacePicker: RIP");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RESULT****", "OK");
            if (requestCode == 1) {
                double latitude = data.getDoubleExtra("LATITUDE", 0.0);
                Log.d("LATITUDE****", "lat" + latitude);
                double longitude = data.getDoubleExtra("LONGITUDE", 0.0);
                Log.d("LONGITUDE****", "" + longitude);
            } else if (requestCode == 2) {
                double latitude = data.getDoubleExtra("LATITUDE", 0.0);
                Log.d("LATITUDE****", "lat" + latitude);
                double longitude = data.getDoubleExtra("LONGITUDE", 0.0);
                Log.d("LONGITUDE****", "" + longitude);
                String address = data.getStringExtra("LOCATION_ADDRESS");
                Log.d("ADDRESS****", address);
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.d("RESULT****", "CANCELLED");
        }
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