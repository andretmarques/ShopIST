package pt.ulisboa.tecnico.cmov.shopist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.GeoApiContext;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


public class MainActivity extends AppCompatActivity implements ListRecyclerAdapter.OnListListener {

    private Animation fromBottom;
    private Animation toBottom;
    private Animation rotateOpen;
    private Animation rotateClose;
    private boolean clicked = false;
    private ExtendedFloatingActionButton createPantryButton;
    private ExtendedFloatingActionButton createShopButton;
    private FloatingActionButton addButton;
    private ArrayList<ItemsList> pantryLists = new ArrayList<>();
    private ArrayList<ItemsList> shoppingLists = new ArrayList<>();
    private RecyclerView pantryListMainRecycler;
    private RecyclerView shoppingListMainRecycler;
    private ListRecyclerAdapter pantryListRecyclerAdapter;
    private ListRecyclerAdapter shoppingListRecyclerAdapter;
    private double actualLongitude;
    private double actualLatitude;
    private DatabaseReference myRef;
    private int listPosition;
    private HashMap<String, String> storeNames = new HashMap<>();

    private String userId;

    protected LocationManager locationManager;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private long pressedTime;
    private GeoApiContext mGeoApiContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), getString(R.string.key_google_apis_android));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        editor = prefs.edit();

        addButton = findViewById(R.id.add_btn);
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
        FirebaseDatabase database = FirebaseDatabase.getInstance("DATABASE_URL");
        myRef = database.getReference();
        userId = getIntent().getStringExtra("UserEmail");
        if (userId != null) {
            editor.putString("userId", userId);
            editor.apply();
        }
        initGoogleMap();
        if (prefs.getString("userId", null) != null){
            userId = prefs.getString("userId", null);
        }

        boolean net = isNetworkAvailable(this.getApplication());

        GPSUpdater mGPS = new GPSUpdater(this.getApplicationContext());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, LocationListenerGPS);
        }

        if (net){
            updateData();
        }else {
            loadDataCache();
        }
        setPantryRecycler(pantryLists);
        setShoppingRecycler(shoppingLists);
        eneableSwipePantry();
        enableSwipeStore();
    }

    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }



    private Boolean isNetworkAvailable(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void updateData(){
        myRef.child("Users").child(userId).child("Pantries").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<ItemsList> t = new GenericTypeIndicator<ItemsList>() {};
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        ItemsList itemsList = singleSnapshot.getValue(t);
                        assert itemsList != null;
                        itemsList.getItemList().remove(null);
                        pantryLists.add(itemsList);
                    }
                    savePantryListToCache();
                }
                setPantryRecycler(pantryLists);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });

        myRef.child("Users").child(userId).child("Stores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<ItemsList> t = new GenericTypeIndicator<ItemsList>() {};
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        ItemsList itemsList = singleSnapshot.getValue(t);
                        shoppingLists.add(itemsList);
                    }
                    saveShoppingListToCache();
                }
                setShoppingRecycler(shoppingLists);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });

        myRef.child("Users").child(userId).child("StoreNames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                    storeNames = dataSnapshot.getValue(t);
                    }
                }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });
    }

    private void initGoogleMap() {

        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.key_google_apis_android))
                    .build();
        }
    }

    public void savePantryListToCache() {
        Gson gson = new Gson();
        String jsonPantry = gson.toJson(pantryLists);
        editor.putString("cachedPantries", jsonPantry);
        editor.apply();
    }

    public void saveShoppingListToCache() {
        Gson gson = new Gson();
        String jsonShopping = gson.toJson(shoppingLists);
        editor.putString("cachedShopping", jsonShopping);
        editor.apply();
    }

    public void saveStoreNamesListToCache() {
        Gson gson = new Gson();
        String jsonShopping = gson.toJson(storeNames);
        editor.putString("cachedStoreNames", jsonShopping);
        editor.apply();
    }

    private void loadDataCache() {
        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonPantry = prefs.getString("cachedPantries", null);
        String jsonShopping = prefs.getString("cachedShopping", null);
        String jsonStores = prefs.getString("cachedStoreNames", null);
        Type type = new TypeToken<ArrayList<ItemsList>>() {}.getType();
        pantryLists = gson.fromJson(jsonPantry, type);
        shoppingLists = gson.fromJson(jsonShopping, type);
        storeNames = gson.fromJson(jsonStores, type);

    }


    private void setPantryRecycler(ArrayList<ItemsList> allLists) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        pantryListMainRecycler.setLayoutManager(layoutManager);
        pantryListRecyclerAdapter = new ListRecyclerAdapter(this, allLists, "PANTRY", this);
        pantryListMainRecycler.setAdapter(pantryListRecyclerAdapter);
    }

    private void setShoppingRecycler(ArrayList<ItemsList> allLists) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        shoppingListMainRecycler.setLayoutManager(layoutManager);
        shoppingListRecyclerAdapter = new ListRecyclerAdapter(this, allLists, "SHOP", this);
        shoppingListRecyclerAdapter.setDirectionsListener(((view, position) -> onClickLocation(shoppingLists.get(position).getLocation())));
        shoppingListMainRecycler.setAdapter(shoppingListRecyclerAdapter);
    }

    private void eneableSwipePantry() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    final ItemsList deletedModel = pantryLists.get(position);
                    final int deletedPosition = position;
                    pantryListRecyclerAdapter.removeItem(position);
                    Snackbar snackbar = Snackbar.make(pantryListMainRecycler, "List " + deletedModel.getName() + " Removed", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("UNDO", (view) -> pantryListRecyclerAdapter.restoreItem(deletedModel, deletedPosition));
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            myRef.child("Users").child(userId).child("Pantries").child(deletedModel.getId()).removeValue();
                        }

                    });
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawable = ContextCompat.getDrawable(MainActivity.this,R.drawable.delete_swipe_layout);
                assert drawable != null;
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();

                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                drawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                drawable.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(pantryListMainRecycler);
    }

    private void enableSwipeStore() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    final ItemsList deletedModel = shoppingLists.get(position);
                    final int deletedPosition = position;
                    shoppingListRecyclerAdapter.removeItem(position);
                    Snackbar snackbar = Snackbar.make(shoppingListMainRecycler, "List " + deletedModel.getName() + " Removed", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("UNDO", (view) -> shoppingListRecyclerAdapter.restoreItem(deletedModel, deletedPosition));
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            myRef.child("Users").child(userId).child("Stores").child(deletedModel.getId()).removeValue();
                            myRef.child("Users").child(userId).child("StoreNames").child(deletedModel.getId()).removeValue();
                            storeNames.remove(deletedModel.getId());
                        }

                    });
                }

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawable = ContextCompat.getDrawable(MainActivity.this,R.drawable.delete_swipe_layout);
                assert drawable != null;
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();

                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                drawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                drawable.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(shoppingListMainRecycler);
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
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }
    };

    public void showCreatePantryPopUp(View v) {
        Intent i = new Intent(this, CreatePantryActivity.class);
        if (actualLatitude != 0.0 && actualLongitude != 0.0 ){
            i.putExtra("EmailUser", userId);
            i.putExtra("ActualLatitude", actualLatitude);
            i.putExtra("ActualLongitude", actualLongitude);
        }
        handleAddMenu();
        startActivityForResult(i, 10001);
    }

    public void showCreateShopPopUp(View v) {
        Intent i = new Intent(this, CreateShopActivity.class);
        i.putExtra("EmailUser", userId);
        if (actualLatitude != 0.0 && actualLongitude != 0.0 ){
            i.putExtra("ActualLatitude", actualLatitude);
            i.putExtra("ActualLongitude", actualLongitude);
        }

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
                pantryList.generateId();
                savePantryListToCache();
                myRef.child("Users").child(userId).child("Pantries").child(pantryList.getId()).setValue(pantryList);
                setPantryRecycler(pantryLists);
            }
            return;
        }
        if (requestCode == 10002) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // get the list of strings here
                ItemsList shoppingList = data.getParcelableExtra("returnedShoppingList");
                shoppingLists.add(shoppingList);
                shoppingList.generateId();
                saveShoppingListToCache();
                myRef.child("Users").child(userId).child("Stores").child(shoppingList.getId()).setValue(shoppingList);
                storeNames.put(shoppingList.getId(), shoppingList.getName());
                saveStoreNamesListToCache();
                myRef.child("Users").child(userId).child("StoreNames").setValue(storeNames);
                shoppingListRecyclerAdapter.notifyDataSetChanged();

            }
            return;
        }
        if (requestCode == 10030) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // get the list of strings here
                ArrayList<Item> itemsPantry = data.getParcelableArrayListExtra("returnedItemList");
                int toBuy = data.getIntExtra("pantryToBuy", 0);
                ItemsList pantry = pantryLists.get(listPosition);
                pantry.setItemList(itemsPantry);
                pantry.setToBuy(toBuy);
                pantryListRecyclerAdapter.notifyItemChanged(listPosition);

            }
            return;
        }

        if (requestCode == 20005) {
            if (resultCode == RESULT_OK) {
                pantryLists = data.getParcelableArrayListExtra("allPantries");
                for (ItemsList il : pantryLists) {
                    int toBuy = 0;
                    for (Item i : il.getItemList()) {
                        toBuy = toBuy + i.getToPurchase();
                    }
                    il.setToBuy(toBuy);
                }
                setPantryRecycler(pantryLists);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shared_pantries_shops, menu);
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.rate:
                onClickShareLove();
                return true;

            case R.id.logout:
                prefs.edit().clear().apply();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            case R.id.shared:
                Intent i = new Intent(MainActivity.this, SharedPantriesShopsActivity.class);
                i.putExtra("UserEmail", userId);
                startActivity(i);
                return true;

            default:
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
            createPantryButton.setVisibility(View.VISIBLE);
            createShopButton.setVisibility(View.VISIBLE);
        } else {
            createShopButton.setClickable(false);
            createPantryButton.setClickable(false);
            createPantryButton.setVisibility(View.GONE);
            createShopButton.setVisibility(View.GONE);

        }
    }

    public void setAnimation(Boolean clicked) {
        if (!clicked) {
            addButton.startAnimation(rotateOpen);
            createPantryButton.startAnimation(fromBottom);
            createShopButton.startAnimation(fromBottom);
        } else {
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
    @Override
    public void onItemClick(int position) {
        if ((pantryListMainRecycler.getVisibility() == View.VISIBLE) && (shoppingListMainRecycler.getVisibility() == View.GONE)) {
            Intent i = new Intent(this, PantryInside.class);
            i.putExtra("pantry", pantryLists.get(position));
            i.putExtra("pantryListName", pantryLists.get(position).getName());
            i.putExtra("pantryListId", pantryLists.get(position).getId());
            i.putExtra("EmailUser", userId);
            listPosition = position;
            startActivityForResult(i, 10030);
        }else if((pantryListMainRecycler.getVisibility() == View.GONE) && (shoppingListMainRecycler.getVisibility() == View.VISIBLE)){
            Intent i = new Intent(this, ShoppingInside.class);
            i.putExtra("userPantryLists", pantryLists);
            i.putExtra("shoppingListName", shoppingLists.get(position).getName());
            i.putExtra("shoppingListId", shoppingLists.get(position).getId());
            i.putExtra("EmailUser", userId);
            startActivityForResult(i, 20005);
        }
    }


    public void onClickShareLove() {
        @SuppressLint("InflateParams") ConstraintLayout contentView = (ConstraintLayout) (this)
                .getLayoutInflater().inflate(R.layout.share_your_love, null);
        ImageView image = contentView.findViewById(R.id.heart);
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
        alertDialog.setOnShowListener(dialogInterface -> animation.start());
        alertDialog.show();
    }


    public Barcode.GeoPoint getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        Barcode.GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new Barcode.GeoPoint((double) (location.getLatitude()),
                    (double) (location.getLongitude()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }

    private void onClickLocation(String location){
        Barcode.GeoPoint addrCoords = getLocationFromAddress(location);
        Uri gmUri = Uri.parse("google.navigation:q=" + addrCoords.lat + "," + addrCoords.lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, gmUri);
        intent.setPackage("com.google.android.apps.maps");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.getApplicationContext().startActivity(intent);
    }


}