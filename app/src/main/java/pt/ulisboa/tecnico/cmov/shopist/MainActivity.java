package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

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


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements ListRecyclerAdapter.OnListListener {

    private Animation fromBottom;
    private Animation toBottom;
    private Animation rotateOpen;
    private Animation rotateClose;
    private boolean clicked = false;
    private ExtendedFloatingActionButton joinButton;
    private ExtendedFloatingActionButton createPantryButton;
    private ExtendedFloatingActionButton createShopButton;
    private FloatingActionButton addButton;
    private final ArrayList<ItemsList> pantryLists = new ArrayList<>();
    private final ArrayList<ItemsList> shoppingLists = new ArrayList<>();
    private RecyclerView pantryListMainRecycler;
    private RecyclerView shoppingListMainRecycler;
    private ListRecyclerAdapter pantryListRecyclerAdapter;
    private ListRecyclerAdapter shoppingListRecyclerAdapter;
    private double actualLongitude;
    private double actualLatitude;
    private DatabaseReference myRef;
    private int listPosition;
    private final HashMap<String, String> storeNames = new HashMap<>();

    String usermail;

    protected LocationManager locationManager;
    GPSUpdater mGPS;
    TextView GPStext;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), getString(R.string.key_google_apis_android));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        editor = prefs.edit();

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
        usermail = getIntent().getStringExtra("UserEmail");
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
            setPantryRecycler(pantryLists);
            setShoppingRecycler(shoppingLists);
        }
        eneableSwipePantry();
        enableSwipeStore();

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
        myRef.child("Users").child(usermail).child("Pantries").addListenerForSingleValueEvent(new ValueEventListener() {
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

                }
                setPantryRecycler(pantryLists);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });

        myRef.child("Users").child(usermail).child("Stores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<ItemsList> t = new GenericTypeIndicator<ItemsList>() {};
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        ItemsList itemsList = singleSnapshot.getValue(t);
                        shoppingLists.add(itemsList);
                    }
                }
                setShoppingRecycler(shoppingLists);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });

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
                            myRef.child("Users").child(usermail).child("Pantries").child(deletedModel.getId()).removeValue();
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
                            myRef.child("Users").child(usermail).child("Stores").child(deletedModel.getId()).removeValue();
                            myRef.child("Users").child(usermail).child("StoreNames").child(deletedModel.getId()).removeValue();
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
            i.putExtra("EmailUser", usermail);
            i.putExtra("ActualLatitude", actualLatitude);
            i.putExtra("ActualLongitude", actualLongitude);
        }
        handleAddMenu();
        startActivityForResult(i, 10001);
    }

    public void showCreateShopPopUp(View v) {
        Intent i = new Intent(this, CreateShopActivity.class);
        i.putExtra("EmailUser", usermail);

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
                myRef.child("Users").child(usermail).child("Pantries").child(pantryList.getId()).setValue(pantryList);


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
                myRef.child("Users").child(usermail).child("Stores").child(shoppingList.getId()).setValue(shoppingList);
                storeNames.put(shoppingList.getId(), shoppingList.getName());
                myRef.child("Users").child(usermail).child("StoreNames").setValue(storeNames);
            }
            return;
        }
        if (requestCode == 10030) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // get the list of strings here
                ArrayList<Item> itemsPantry = data.getParcelableArrayListExtra("returnedItemList");
                ItemsList pantry = pantryLists.get(listPosition);
                pantry.setItemList(itemsPantry);
                pantryListRecyclerAdapter.notifyItemChanged(listPosition);

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

            case R.id.logout:
                prefs.edit().clear().apply();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

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

    @Override
    public void onItemClick(int position) {
        if ((pantryListMainRecycler.getVisibility() == View.VISIBLE) && (shoppingListMainRecycler.getVisibility() == View.GONE)) {
            Intent i = new Intent(this, PantryInside.class);
            i.putExtra("pantryListName", pantryLists.get(position).getName());
            i.putExtra("pantryListId", pantryLists.get(position).getId());
            i.putExtra("EmailUser", usermail);
            listPosition = position;
            startActivityForResult(i, 10030);
        }else if((pantryListMainRecycler.getVisibility() == View.GONE) && (shoppingListMainRecycler.getVisibility() == View.VISIBLE)){
            Intent i = new Intent(this, ShoppingInside.class);
            i.putExtra("userPantryLists", pantryLists);
            i.putExtra("shoppingListName", shoppingLists.get(position).getName());
            i.putExtra("shoppingListId", shoppingLists.get(position).getId());
            i.putExtra("EmailUser", usermail);
            startActivity(i);
        }
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