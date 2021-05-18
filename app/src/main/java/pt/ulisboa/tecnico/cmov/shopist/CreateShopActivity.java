package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.Duration;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CreateShopActivity extends AppCompatActivity {
    private String locationPicked;
    private TextView textView;
    private TextView listLocation;
    private double currentLatitude;
    private double currentLongitude;
    private Duration eta;


    private GeoApiContext mGeoApiContext = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Places.initialize(getApplicationContext(),getString(R.string.key_google_apis_android));
        setContentView(R.layout.new_shop_layout);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        textView = findViewById(R.id.list_name);
        listLocation = findViewById(R.id.list_location);
        Intent i = getIntent();
        initGoogleMap();

        currentLatitude = i.getDoubleExtra("ActualLatitude", 0);
        currentLongitude = i.getDoubleExtra("ActualLongitude", 0);


    }

    public void setCancelButton(View v){
        finish();
    }

    public void setCreateButton(View v){
        if (textView.getText().toString().equals("")) {
            textView.setError("Name should not be empty");
        }
        else {
            String listName = textView.getText().toString();
            ItemsList newList = new ItemsList(listName, ItemsList.ListType.SHOP);
            newList.setLocation(locationPicked);
            locationPicked = "";
            if (eta == null){
                newList.setEta("");
            }else {
                newList.setEta(eta.toString());
            }
            Intent intent = new Intent();
            intent.putExtra("returnedShoppingList", newList);
            setResult(MainActivity.RESULT_OK, intent);
            finish();
        }
    }

    public void setLocationPicked(View v){
        showPlacePicker();
    }


    private void showPlacePicker() {
        double initialLat = 38.736982568082674;
        double initialLong = -9.302610416802459;
        if(currentLatitude != 0 && currentLongitude != 0) {
            initialLat = currentLatitude;
            initialLong = currentLongitude;
        }
        Intent intent = new PlacePicker.IntentBuilder()
                .setLatLong(initialLat, initialLong)  // Initial Latitude and Longitude the Map will load into
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
                .build(CreateShopActivity.this);
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
                if(addressData != null) {
                    locationPicked = getRoad(addressData.getLatitude(), addressData.getLongitude());
                }else {
                    locationPicked = "";
                }

                listLocation.setText(locationPicked);
                listLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
                if(currentLatitude != 0 && currentLongitude != 0) {
                    calculateDirections(addressData);
                }


            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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

    private void initGoogleMap() {

        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.key_google_apis_android))
                    .build();
        }
    }

    private void calculateDirections(AddressData addressData){
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                addressData.getLatitude(),
                addressData.getLongitude()
        );

        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.origin(
                new com.google.maps.model.LatLng(
                        currentLatitude,
                        currentLongitude));

        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                eta = result.routes[0].legs[0].duration;
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("TAG", "onFailure: " + e.getMessage() );

            }
        });

    }
}
