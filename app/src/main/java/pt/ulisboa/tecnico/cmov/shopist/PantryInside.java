package pt.ulisboa.tecnico.cmov.shopist;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.core.UserWriteRecord;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class PantryInside extends AppCompatActivity implements ItemRecyclerAdapter.OnItemListener {
    private ArrayList<Item> itemsPantry = new ArrayList<>();
    private ArrayList<PublicItem> listPublic = new ArrayList<>();
    private DatabaseReference myRef;
    private String pantryId;
    Button scanBarcodeBtn;
    String barcode = "";
    Double price;
    String shop;
    String messageAll = "";
    private int listPosition;
    private RecyclerView productsMainRecycler;
    private ItemRecyclerAdapter itemRecyclerAdapter;
    private HashMap<String, String> positionsMap = new HashMap<>();
    String userId;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_pantry);
        setSupportActionBar(findViewById(R.id.toolbar_pantry));
        ActionBar actionBar = getSupportActionBar();
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_pantry_title);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        boolean net = isNetworkAvailable(this.getApplication());
        userId = getIntent().getStringExtra("EmailUser");

        Bundle b = getIntent().getExtras();
        if(b != null){
            actionTitle = b.getString("pantryListName");
            actionTitle = "Pantry: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            pantryId = b.getString("pantryListId");
            itemsPantry = b.getParcelableArrayList("pantryList");
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setItemsRecycler(itemsPantry);


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("returnedItemList", itemsPantry);
                setResult(PantryInside.RESULT_OK, intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        scanBarcodeBtn = findViewById(R.id.scan_barcode);
        scanBarcodeBtn.setOnClickListener(view -> startActivityForResult(new Intent(PantryInside.this, ScanBarcodeActivity.class), 10025));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pantry_menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PantryInside.this);
        alertDialog.setTitle("Share this pantry");
        alertDialog.setMessage("Enter your friend's email");
        final EditText input = new EditText(PantryInside.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String email = input.getText().toString();
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(getApplicationContext(), "Wrong email format", Toast.LENGTH_SHORT).show();
                            onOptionsItemSelected(item);

                        }
                        Toast.makeText(getApplicationContext(), "Your friend has now access to the pantry", Toast.LENGTH_SHORT).show();
                        sharePantry(email);
                    }
                });

        alertDialog.setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
        return true;
    }

    public void sharePantry(String email){
        SharedPantry newSharedPantry = new SharedPantry(pantryId, mAuth.getCurrentUser().getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.child("Users").getChildren()) {
                    if (singleSnapshot.child("email").getValue().toString().equals(email)) {
                        myRef.child("Users").child(singleSnapshot.getKey()).child("SharedPantries").child(pantryId).setValue(newSharedPantry);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

    private void populateLists(){
        myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<Item> t = new GenericTypeIndicator<Item>() {};
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Item item = singleSnapshot.getValue(t);
                        itemsPantry.add(item);
                        assert item != null;
                        if(!positionsMap.containsKey(item.getId()))
                            positionsMap.put(item.getId(), String.valueOf(itemsPantry.indexOf(item)));
                    }

                }
                setItemsRecycler(itemsPantry);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });
    }

    private void populatePositionMap(){
        positionsMap = new HashMap<>();
        for (Item i : itemsPantry){
            positionsMap.put(i.getId(), String.valueOf(itemsPantry.indexOf(i)));
        }
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "P", this);
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
        populatePositionMap();
        enableSwipeLeft();
        enableSwipeRight();

    }

    public void createItem(View view) {
        Intent intent = new Intent(this, CreateProductActivity.class);
        intent.putExtra("EmailUser", userId);
        startActivityForResult(intent, 10015);
    }

    private void showAlert(Item product){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PantryInside.this)
                .setTitle("ERROR")
                .setMessage("You already have " + product.getName() + " in your pantry with another store")
                .setPositiveButton("OK", null);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 10057){
            if (resultCode == RESULT_OK) {
                Item newItem = data.getParcelableExtra("returnedItem");
                myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").child(positionsMap.get(newItem.getId())).setValue(newItem);
                itemsPantry.set(listPosition, newItem);
                itemRecyclerAdapter.notifyItemChanged(listPosition);
                Snackbar snackbar = Snackbar.make(productsMainRecycler, "Product " + newItem.getName() + " updated", 1000);
                snackbar.show();
            }
        }
        else if (requestCode == 10015) {
            if (resultCode == RESULT_OK) {
                Item newItem = data.getParcelableExtra("returnedProduct");
                Item inList = null;

                if(itemsPantry.contains(newItem)) {
                    inList = itemsPantry.get(itemsPantry.indexOf(newItem));
                }

                if(inList != null && !newItem.getShops().keySet().equals(inList.getShops().keySet())){
                  showAlert(inList);
                }else if(inList != null && newItem.getShops().keySet().equals(inList.getShops().keySet())) {
                    inList.setQuantity(inList.getQuantity() + newItem.getQuantity());
                    myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").child(positionsMap.get(inList.getId())).child("quantity").setValue(inList.getQuantity());
                }else if(inList == null || !newItem.getShops().keySet().equals(inList.getShops().keySet())) {
                    itemsPantry.add(newItem);
                    myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").setValue(itemsPantry);
                }

                setItemsRecycler(itemsPantry);
                populatePositionMap();
            }
            return;
        } else if (requestCode == 10025) {
            if (resultCode == RESULT_OK) {
                barcode = data.getStringExtra("Barcode");
                boolean in = false;
                for (Item item : itemsPantry) {
                    if (item.getProductBarcode().equals(barcode)) {
                        Intent i = new Intent(this, EditProductActivity.class);
                        i.putExtra("product", item);
                        i.putExtra("UserId", userId);
                        i.putExtra("PantryId", pantryId);
                        startActivityForResult(i, 10057);
                        in = true;
                        break;
                    }
                }
                if (!in) {
                    Toast.makeText(PantryInside.this, "You don't have a product with this barcode. Please create one", Toast.LENGTH_LONG).show();
                }
            }

        }

        /*
        else if (requestCode == 10025) {
            if (resultCode == RESULT_OK) {
                barcode = data.getStringExtra("Barcode");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listPublic = (ArrayList<PublicItem>) snapshot.child("PublicItems").child(barcode).getValue();
                        if (listPublic == null) { listPublic = new ArrayList<>(); }

                        if (!snapshot.child("PublicItems").child(barcode).exists()) {
                            ScanBarcodeAssist();
                        }
                        else {
                            if (listPublic.size() > 1) {
                                messageAll = "";
                                for (DataSnapshot singleSnapshot : snapshot.child("PublicItems").child(barcode).getChildren()) {
                                    messageAll = messageAll + "Shop: " +
                                            singleSnapshot.child("shop").getValue()
                                            + "\n" + "Price: " +
                                            singleSnapshot.child("price").getValue() + "€"
                                            + "\n\n";
                                }
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PantryInside.this)
                                        .setTitle("Product " + barcode)
                                        .setMessage(messageAll)
                                        .setNegativeButton("Thank you", null)
                                        .setPositiveButton("Add new shop", (dialogInterface, i) -> ScanBarcodeAssist());
                                alertDialogBuilder.show();
                                messageAll = "";
                            }
                            else {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PantryInside.this)
                                        .setTitle("Product " + barcode)
                                        .setMessage("Shop: " + snapshot.child("PublicItems").child(barcode).child("0").child("shop").getValue()
                                                + "\n" + "Price: " + snapshot.child("PublicItems").child(barcode).child("0").child("price").getValue() + "€")
                                        .setNegativeButton("Thank you", null)
                                        .setPositiveButton("Add new shop", (dialogInterface, i) -> ScanBarcodeAssist());
                                alertDialogBuilder.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            return;
        }
        if (requestCode == 20221) {
            if (resultCode == RESULT_OK) {
                price = data.getDoubleExtra("price", 0);
                shop = data.getStringExtra("shop");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exists = false;
                        for (DataSnapshot singleSnapshot : snapshot.child("PublicItems").child(barcode).getChildren()) {
                            if (singleSnapshot.child("shop").getValue().toString().equals(shop)) {
                                Toast.makeText(getApplicationContext(), "This shop already has this item and price for it", Toast.LENGTH_LONG).show();
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            PublicItem newPublicItem = new PublicItem(barcode, price, shop);
                            if (listPublic != null) {
                                listPublic.add(newPublicItem);
                                myRef.child("PublicItems").child(barcode).setValue(listPublic);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            return;
        }*/
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void ScanBarcodeAssist() {
        Intent i = new Intent(PantryInside.this, PriceShopActivity.class);
        i.putExtra("barcode", barcode);
        startActivityForResult(i, 20221);
    }

    private void enableSwipeLeft(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                final Item swipedItem = itemsPantry.get(position);
                if (swipedItem.getQuantity() == 0) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                final Item swipedItem = itemsPantry.get(position);


                if (direction == ItemTouchHelper.LEFT) {
                    final int deletedPosition = position;
                    itemRecyclerAdapter.removeItem(position);
                    Snackbar snackbar = Snackbar.make(productsMainRecycler, "" + swipedItem.getName() + " Removed", 1250);
                    snackbar.setAction("UNDO", (view) -> itemRecyclerAdapter.restoreItem(swipedItem, deletedPosition));
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            //myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").child(positionsMap.get(swipedItem.getId())).removeValue();
                            myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").setValue(itemsPantry);
                            populatePositionMap();
                        }

                    });
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawableLeft = ContextCompat.getDrawable(PantryInside.this, R.drawable.ic_baseline_delete_24);
                final ColorDrawable background;
                assert drawableLeft != null;
                int intrinsicWidthCon = drawableLeft.getIntrinsicWidth();
                int intrinsicHeightCon = drawableLeft.getIntrinsicHeight();
                background = new ColorDrawable(Color.parseColor("#cc0000"));

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + 20, itemView.getBottom());

                background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());

                int iconTop = itemView.getTop() + (itemHeight - intrinsicHeightCon) / 2;
                int IconMargin = (itemHeight - intrinsicHeightCon) / 2;
                int iconLeft = itemView.getRight() - IconMargin - intrinsicWidthCon;
                int iconRight = itemView.getRight() - IconMargin;
                int iconBottom = iconTop + intrinsicHeightCon;

                drawableLeft.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                drawableLeft.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX / 2, dY, actionState, isCurrentlyActive);


            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(productsMainRecycler);
    }

    private void enableSwipeRight() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                final Item swipedItem = itemsPantry.get(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    Snackbar snackbar = Snackbar.make(productsMainRecycler, "One " + swipedItem.getName() + " Added", 1000);
                    itemRecyclerAdapter.addQuantity(swipedItem, position);
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            myRef.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").child(String.valueOf(position))
                                    .child("quantity").setValue(swipedItem.getQuantity());
                        }
                    });
                }
                itemRecyclerAdapter.notifyItemChanged(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawableRight = ContextCompat.getDrawable(PantryInside.this, R.drawable.ic_baseline_add_24);
                final ColorDrawable background;
                assert drawableRight != null;
                int intrinsicWidthAdd = drawableRight.getIntrinsicWidth();
                int intrinsicHeightAdd = drawableRight.getIntrinsicHeight();
                background = new ColorDrawable(Color.parseColor("#12752c"));

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + 20, itemView.getBottom());

                int iconTop = itemView.getTop() + (itemHeight - intrinsicHeightAdd) / 2;
                int iconLeft = (itemHeight - intrinsicHeightAdd) / 2;
                int iconRight = iconLeft + intrinsicWidthAdd;
                int iconBottom = iconTop + intrinsicHeightAdd;
                drawableRight.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                drawableRight.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX / 2, dY, actionState, isCurrentlyActive);

                }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(productsMainRecycler);
    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(this, EditProductActivity.class);
        i.putExtra("product", itemsPantry.get(position));
        i.putExtra("UserId", userId);
        i.putExtra("PantryId", pantryId);
        listPosition = position;
        startActivityForResult(i, 10057);

    }
}
