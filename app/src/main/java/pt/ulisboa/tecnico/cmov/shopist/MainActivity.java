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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity{

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.add_btn);
        joinButton = findViewById(R.id.join_btn);
        createButton = findViewById(R.id.create_btn);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);

        mGPS = new GPSUpdater(this.getApplicationContext());

        GPStext = findViewById(R.id.GPSRoad);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("OLA", "Permission");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationListenerGPS);
        }
    }
    LocationListener LocationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("OLA", "onLocationChanged: "+location);
            String address = getRoad(location.getLatitude(), location.getLongitude());
            GPStext.setText(address);
        }
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude","disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude","enable");
        }
    };

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

    public String getRoad(double latitude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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