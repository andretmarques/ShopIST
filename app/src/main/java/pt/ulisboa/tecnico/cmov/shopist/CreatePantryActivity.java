package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CreatePantryActivity extends AppCompatActivity {
    private String locationPicked;
    private TextView textView;
    private TextView listLocation;
    private Intent i;

    String emailuser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getApplicationContext(),getString(R.string.key_google_apis_android));
        setContentView(R.layout.new_pantry_layout);
        setSupportActionBar(findViewById(R.id.toolbar_main));
        i = getIntent();
        processButton();

        textView = findViewById(R.id.list_name);
        listLocation = findViewById(R.id.list_location);
        emailuser = getIntent().getStringExtra("EmailUser");


    }

    public void processButton(){
        Button actualLoc = findViewById(R.id.actual_location);
        if (!i.hasExtra("ActualLatitude") || !i.hasExtra("ActualLatitude")){
            actualLoc.setEnabled(false);
        }
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
            ItemsList newList = new ItemsList(listName, ItemsList.ListType.PANTRY);
            newList.setLocation(locationPicked);
            locationPicked = "";
            Intent intent = new Intent();
            intent.putExtra("returnedPantryList", newList);
            setResult(MainActivity.RESULT_OK, intent);
            finish();
        }
    }

    public void setLocationPicked(View v){
        showPlacePicker();
    }

    public void setActualLocation(View v){

        double latitude = i.getDoubleExtra("ActualLatitude", 0);
        double longitude = i.getDoubleExtra("ActualLongitude", 0);
        String home = getRoad(latitude, longitude);
        locationPicked = home;
        listLocation.setText(home);
        listLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);


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
                .build(CreatePantryActivity.this);
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
                if(addressData != null)
                    locationPicked = getRoad(addressData.getLatitude(), addressData.getLongitude());
                    listLocation.setText(locationPicked);
                    listLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);

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


}
