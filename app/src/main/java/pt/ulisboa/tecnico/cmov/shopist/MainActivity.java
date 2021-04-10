package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ViewAnimator;

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

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


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
                assert data != null;
                AddressData addressData = data.getParcelableExtra("ADDRESS_INTENT");
                Log.d("DDDDDDDDDOOOOONNNNEEEE", "onActivityResult: " + addressData.toString());
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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


    public void onClickShareLove(MenuItem item) {
        @SuppressLint("InflateParams") ConstraintLayout contentView = (ConstraintLayout) (this)
                .getLayoutInflater().inflate(R.layout.share_your_love, null);

        ImageView image = (ImageView) contentView.findViewById(R.id.heart);
        final AnimatedVectorDrawable animation = (AnimatedVectorDrawable) image.getDrawable();

        final KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.RED, Color.BLUE, 16711935)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(450, 5000L);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(contentView)
                .setNegativeButton(R.string.close_button_love, null)
                .setMessage("We appreciate your love!")
                .setTitle("Thanks");

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                    animation.start();
            }
        });
        alertDialog.show();
    }
}