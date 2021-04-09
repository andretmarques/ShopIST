package pt.ulisboa.tecnico.cmov.shopist;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSUpdater extends AppCompatActivity implements LocationListener{
    private final Context mcontext;
    MainActivity mainActivity;
    TextView GPStxt;
    protected LocationManager lm;

    public GPSUpdater(Context context){
        this.mcontext=context;
    }


    @Override
        public void onLocationChanged(Location location) {
            Log.d("OLA", "onLocationChanged: "+location);
            String address = getRoad(location.getLatitude(), location.getLongitude());
            GPStxt.setText(address);
        }
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude","disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude","enable");
        }

    public String getRoad(double latitude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(mcontext, Locale.getDefault());
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