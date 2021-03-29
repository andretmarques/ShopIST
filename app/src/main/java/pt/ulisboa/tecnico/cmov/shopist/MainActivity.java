package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    GPSLocation mGPS = new GPSLocation(this);
    TextView text = findViewById(R.id.GPSRoad);

    public FusedLocationProviderClient mFusedLocationClient;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    String addressLine = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.add_btn);
        joinButton = findViewById(R.id.join_btn);
        createButton = findViewById(R.id.create_btn);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void onClickButton(View v) {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    public void setVisibility(Boolean clicked) {
        if (!clicked) {
            joinButton.setVisibility(View.VISIBLE);
            createButton.setVisibility(View.VISIBLE);
        } else {
            joinButton.setVisibility(View.INVISIBLE);
            createButton.setVisibility(View.INVISIBLE);

        }
    }

    public void setAnimation(Boolean clicked) {
        if (!clicked) {
            addButton.startAnimation(rotateOpen);
            joinButton.startAnimation(fromBottom);
            createButton.startAnimation(fromBottom);
        } else {
            joinButton.startAnimation(toBottom);
            createButton.startAnimation(toBottom);
            addButton.startAnimation(rotateClose);
        }
    }

    public void updateLocation(View v) {
        if(mGPS.canGetLocation ){
            mGPS.getLocation();
            text.setText("Lat "+mGPS.getLatitude()+"Lon "+mGPS.getLongitude());
        }else{
            text.setText("Unable to find location");
            System.out.println("Unable");
        }
    }

    public void getLocationPermission(View view) {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                @Override
                public void onComplete(@NonNull Task<android.location.Location> task) {
                    double globalLatitude = 0, globalLongitude = 0;
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        globalLatitude = location.getLatitude();
                        globalLongitude = location.getLongitude();
                        Log.d("INFO", "onComplete: latitude " + globalLatitude);
                        Log.d("INFO", "onComplete: longitude " + globalLongitude);
                    }
                    addressLine = getRoadName(globalLatitude, globalLongitude);
                }
            });
        }
        Log.d("INFO", "onComplete: address " + addressLine);
    }

        public String getRoadName(double latitude, double longitude) {
            String myCity = "";
            String address = "";
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            Log.d("INFO", "road: latitude " + latitude);
            Log.d("INFO", "road: longitude " + longitude);

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude,1);
                address = addresses.get(0).getAddressLine(0);
                Log.d("TAG", "Address: " + address);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return address;
        }


}