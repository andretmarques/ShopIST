package pt.ulisboa.tecnico.cmov.shopist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private Animation fromBottom;
    private Animation toBottom;
    private Animation rotateOpen;
    private Animation rotateClose;
    private boolean clicked = false;
    ExtendedFloatingActionButton joinButton;
    ExtendedFloatingActionButton createButton;
    FloatingActionButton addButton;

    private FusedLocationProviderClient fusedLocationClient;


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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

    }

    public FusedLocationProviderClient getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                return;
                            }
                        }
                    });
        }
        return fusedLocationClient;
    }



    public void onClickButton(View v){
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    public void setVisibility(Boolean clicked){
        if(!clicked) {
            joinButton.setVisibility(View.VISIBLE);
            createButton.setVisibility(View.VISIBLE);
        }
        else{
            joinButton.setVisibility(View.INVISIBLE);
            createButton.setVisibility(View.INVISIBLE);

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
}