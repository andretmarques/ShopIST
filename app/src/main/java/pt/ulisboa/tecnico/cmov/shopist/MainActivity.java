package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Animation fromBottom;
    private Animation toBottom;
    private Animation rotateOpen;
    private Animation rotateClose;
    private boolean clicked = false;
    ExtendedFloatingActionButton joinButton;
    ExtendedFloatingActionButton createPantryButton;
    ExtendedFloatingActionButton createShopButton;
    FloatingActionButton addButton;
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView listLists;
    private ArrayList<ItemsList> pantryLists;
    private ArrayList<ItemsList> shoppingLists;
    RecyclerView pantryListMainRecycler;
    RecyclerView shoppingListMainRecycler;
    ListRecyclerAdapter pantryListRecyclerAdapter;
    ListRecyclerAdapter shoppingListRecyclerAdapter;
    Button viewPantries;
    private String locationPicked;
    private double actualLongitude;
    private double actualLatitude;
    private DatabaseReference myRef;



    protected LocationManager locationManager;
    GPSUpdater mGPS;
    TextView GPStext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), getString(R.string.key_google_apis_android));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        addButton = findViewById(R.id.add_btn);
        joinButton = findViewById(R.id.join_btn);
        createPantryButton = findViewById(R.id.create_pantry_btn);
        createShopButton = findViewById(R.id.create_shop_btn);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        pantryListMainRecycler = findViewById(R.id.pantry_recycler);
        shoppingListMainRecycler = findViewById(R.id.shopping_recycler);

        shoppingListMainRecycler.setVisibility(View.GONE);
        pantryListMainRecycler.setVisibility(View.VISIBLE);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        boolean net = isNetworkAvailable(this.getApplication());


        mGPS = new GPSUpdater(this.getApplicationContext());

        GPStext = findViewById(R.id.GPSRoad);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, LocationListenerGPS);
        }
        if (net){
            updateData();
        }else{
            pantryLists = new ArrayList<>();
            shoppingLists = new ArrayList<>();
            setPantryRecycler(pantryLists);
            setShoppingRecycler(shoppingLists);

        }


    }

    private Boolean isNetworkAvailable(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    private void updateData(){
        myRef.child("Pantries").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<ItemsList>> t = new GenericTypeIndicator<ArrayList<ItemsList>>() {};
                    pantryLists = dataSnapshot.getValue(t);
                }
                else{
                    pantryLists = new ArrayList<>();
                }
                setPantryRecycler(pantryLists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });

        myRef.child("Stores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<ItemsList>> t = new GenericTypeIndicator<ArrayList<ItemsList>>() {};
                    shoppingLists = dataSnapshot.getValue(t);
                }
                else{
                    shoppingLists = new ArrayList<>();
                }
                setShoppingRecycler(shoppingLists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });
    }


    private void setPantryRecycler(List<ItemsList> allLists) {

        pantryListMainRecycler = findViewById(R.id.pantry_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        pantryListMainRecycler.setLayoutManager(layoutManager);
        pantryListRecyclerAdapter = new ListRecyclerAdapter(this, allLists, "PANTRY");
        pantryListMainRecycler.setAdapter(pantryListRecyclerAdapter);
    }

    private void setShoppingRecycler(List<ItemsList> allLists) {

        shoppingListMainRecycler = findViewById(R.id.shopping_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        shoppingListMainRecycler.setLayoutManager(layoutManager);
        shoppingListRecyclerAdapter = new ListRecyclerAdapter(this, allLists, "SHOP");
        shoppingListMainRecycler.setAdapter(shoppingListRecyclerAdapter);
    }

    public void showPantries(View v){
        shoppingListMainRecycler.setVisibility(View.GONE);
        pantryListMainRecycler.setVisibility(View.VISIBLE);

    }

    public void showStores(View v){
        pantryListMainRecycler.setVisibility(View.GONE);
        shoppingListMainRecycler.setVisibility(View.VISIBLE);
    }

    LocationListener LocationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String address = getRoad(location.getLatitude(), location.getLongitude());
            actualLatitude = location.getLatitude();
            actualLongitude = location.getLongitude();
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

    public void showCreatePantryPopUp(View v) {
        Intent i = new Intent(this, CreatePantryActivity.class);
        if (actualLatitude != 0.0 && actualLongitude != 0.0 ){
            i.putExtra("ActualLatitude", actualLatitude);
            i.putExtra("ActualLongitude", actualLongitude);
        }
        handleAddMenu();
        startActivityForResult(i, 10001);
    }

    public void showCreateShopPopUp(View v) {
        Intent i = new Intent(this, CreateShopActivity.class);

        handleAddMenu();
        startActivityForResult(i, 10002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 10001) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // get the list of strings here
                ItemsList pantryList = data.getParcelableExtra("returnedPantryList");
                pantryLists.add(pantryList);
                myRef.child("Pantries").setValue(pantryLists);

            }
            return;
        }
        if (requestCode == 10002) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // get the list of strings here
                ItemsList shoppingList = data.getParcelableExtra("returnedShoppingList");
                shoppingLists.add(shoppingList);
                myRef.child("Stores").setValue(shoppingLists);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

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
                //onClickShareLove();
                Log.d("TAG", "onOptionsItemSelected: Rate");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (clicked) {
                Rect outRectAdd = new Rect();
                Rect outRectPantry= new Rect();
                Rect outRectShop= new Rect();
                Rect outRectCreate = new Rect();
                addButton.getGlobalVisibleRect(outRectAdd);
                createPantryButton.getGlobalVisibleRect(outRectPantry);
                createShopButton.getGlobalVisibleRect(outRectShop);
                joinButton.getGlobalVisibleRect(outRectCreate);
                if (!outRectAdd.contains((int) event.getRawX(), (int) event.getRawY())
                        && !outRectPantry.contains((int) event.getRawX(), (int) event.getRawY()) &&
                        !outRectShop.contains((int) event.getRawX(), (int) event.getRawY()) &&
                        !outRectCreate.contains((int) event.getRawX(), (int) event.getRawY())) {
                    handleAddMenu();
                    return false;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    private void handleAddMenu() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    public void onClickButton(View v) {
        handleAddMenu();
    }

    public void setVisibility(Boolean clicked) {
        if (!clicked) {
            createShopButton.setClickable(true);
            createPantryButton.setClickable(true);
            joinButton.setVisibility(View.VISIBLE);
            createPantryButton.setVisibility(View.VISIBLE);
            createShopButton.setVisibility(View.VISIBLE);
        } else {
            createShopButton.setClickable(false);
            createPantryButton.setClickable(false);
            joinButton.setVisibility(View.GONE);
            createPantryButton.setVisibility(View.GONE);
            createShopButton.setVisibility(View.GONE);

        }
    }

    public void setAnimation(Boolean clicked) {
        if (!clicked) {
            addButton.startAnimation(rotateOpen);
            joinButton.startAnimation(fromBottom);
            createPantryButton.startAnimation(fromBottom);
            createShopButton.startAnimation(fromBottom);
        } else {
            joinButton.startAnimation(toBottom);
            createPantryButton.startAnimation(toBottom);
            createShopButton.startAnimation(toBottom);
            addButton.startAnimation(rotateClose);
        }
    }

    public String getRoad(double latitude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0).getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }


   /* public void onClickShareLove() {
        @SuppressLint("InflateParams") ConstraintLayout contentView = (ConstraintLayout) (this)
                .getLayoutInflater().inflate(R.layout.share_your_love, null);

        ImageView image = (ImageView) contentView.findViewById(R.id.heart);
        final AnimatedVectorDrawable animation = (AnimatedVectorDrawable) image.getDrawable();

        final KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
        konfettiView.build()
                .addColors(Color.RED, Color.GREEN, Color.YELLOW, 16753920, 15631086)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(1200L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(600, 1200L);


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
    */
}