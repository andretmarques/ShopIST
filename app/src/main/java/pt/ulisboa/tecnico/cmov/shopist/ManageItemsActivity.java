package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Application;
import android.content.Context;
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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageItemsActivity extends AppCompatActivity{
    private ArrayList<Item> itemsPantry;
    private final ArrayList<Item> allItems = new ArrayList<>();
    private RecyclerView productsMainRecycler;
    private final String quantity = "1";
    private ItemRecyclerAdapter itemRecyclerAdapter;
    private DatabaseReference myRef;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_items);
        setSupportActionBar(findViewById(R.id.toolbar_allItems));
        ActionBar actionBar = getSupportActionBar();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        boolean net = isNetworkAvailable(this.getApplication());

        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsPantry = b.getParcelableArrayList("pantryItems");
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (net){
            updateData();
        }else {
            setItemsRecycler(allItems);
        }
        enableSwipeProduct();
    }

    private void updateData() {
        myRef.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<Item> t = new GenericTypeIndicator<Item>() {
                    };
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Item item = singleSnapshot.getValue(t);
                        allItems.add(item);
                    }
                }
                setItemsRecycler(allItems);
                enableSwipeProduct();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
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
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }


    private void setItemsRecycler(ArrayList<Item> products) {
        productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "ALL");
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    private void enableSwipeProduct() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.RIGHT) {
                    final Item swipedItem = allItems.get(position);

                    if(itemsPantry.contains(swipedItem)){
                        int index = itemsPantry.indexOf(swipedItem);
                        itemsPantry.get(index).addQuantity(Integer.parseInt(quantity));
                    }else {
                        swipedItem.setQuantity(Integer.parseInt(quantity));
                        itemsPantry.add(swipedItem);
                    }

                    Snackbar snackbar = Snackbar.make(productsMainRecycler, "One " + swipedItem.getName() + " Added", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    itemRecyclerAdapter.notifyItemChanged(position);
                    }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawable = ContextCompat.getDrawable(ManageItemsActivity.this,R.drawable.add_swipe_layout);
                final ColorDrawable background = new ColorDrawable(Color.parseColor("#12752c"));
                assert drawable != null;
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + 20, itemView.getBottom());

                int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int iconLeft = (itemHeight - intrinsicHeight) / 2;
                int iconRight = iconLeft + intrinsicWidth;
                int iconBottom = iconTop + intrinsicHeight;
                drawable.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                drawable.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX/2, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(productsMainRecycler);
    }

    public void confirmAdd(View view){
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("returnedList", itemsPantry);
        setResult(PantryInside.RESULT_OK, intent);
        finish();
    }

    public void createItem(View view){
        Intent intent = new Intent(this, CreateProductActivity.class);
        startActivityForResult(intent, 10015);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enableSwipeProduct();
        if (requestCode == 10015) {
            if (resultCode == RESULT_OK) {
                Item newItem = data.getParcelableExtra("returnedProduct");
                allItems.add(newItem);
                myRef.child("Products").child(newItem.getId()).setValue(newItem);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
