package pt.ulisboa.tecnico.cmov.shopist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SharedPantriesShopsActivity extends AppCompatActivity implements ListRecyclerAdapter.OnListListener {

    private final ArrayList<ItemsList> sharedPantryLists = new ArrayList<>();
    private final ArrayList<ItemsList> sharedShopLists = new ArrayList<>();
    private DatabaseReference myRef;
    private String userId;
    private RecyclerView pantryListMainRecycler;
    private RecyclerView shoppingListMainRecycler;
    private ListRecyclerAdapter pantryListRecyclerAdapter;
    private int listPosition;
    private ArrayList<String> ownerIds = new ArrayList<>();
    String owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_pantries_shops);
        setSupportActionBar(findViewById(R.id.toolbar_shared));
        FirebaseDatabase database = FirebaseDatabase.getInstance("DATABASE_URL");
        myRef = database.getReference();

        userId = getIntent().getStringExtra("UserEmail");

        pantryListMainRecycler = findViewById(R.id.pantry_recycler);
        shoppingListMainRecycler = findViewById(R.id.shopping_recycler);

        getSharedPantries();

        enableSwipePantry();

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.single_pantries_shops, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.single) {
            Intent i = new Intent(SharedPantriesShopsActivity.this, MainActivity.class);
            i.putExtra("UserEmail", userId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showPantries(View v){
        shoppingListMainRecycler.setVisibility(View.GONE);
        pantryListMainRecycler.setVisibility(View.VISIBLE);
    }

    public void showStores(View v){
        pantryListMainRecycler.setVisibility(View.GONE);
        shoppingListMainRecycler.setVisibility(View.VISIBLE);
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
        ListRecyclerAdapter shoppingListRecyclerAdapter = new ListRecyclerAdapter(this, allLists, "SHOP", this);
        shoppingListMainRecycler.setAdapter(shoppingListRecyclerAdapter);
    }

    private void enableSwipePantry() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    final ItemsList deletedModel = sharedPantryLists.get(position);
                    final int deletedPosition = position;
                    pantryListRecyclerAdapter.removeItem(position);
                    Snackbar snackbar = Snackbar.make(pantryListMainRecycler, "List " + deletedModel.getName() + " Removed", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("UNDO", (view) -> pantryListRecyclerAdapter.restoreItem(deletedModel, deletedPosition));
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            myRef.child("Users").child(userId).child("SharedPantries").child(deletedModel.getId()).removeValue();
                        }

                    });
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawable = ContextCompat.getDrawable(SharedPantriesShopsActivity.this,R.drawable.delete_swipe_layout);
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

    public void getSharedPantries() {
        myRef.child("Users").child(userId).child("SharedPantries").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sharedPantryLists.clear();
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    String pantryId = singleSnapshot.child("pantryId").getValue().toString();
                    String ownerId = singleSnapshot.child("ownerId").getValue().toString();
                    ownerIds.add(ownerId);
                    owner = ownerId;
                    myRef.child("Users").child(ownerId).child("Pantries").child(pantryId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            myRef.child("Users").child(ownerId).child("Pantries").child(pantryId).child("itemList").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            sharedPantryLists.add(snapshot.getValue(ItemsList.class));
                            setPantryRecycler(sharedPantryLists);
                            shoppingListMainRecycler.setVisibility(View.GONE);
                            pantryListMainRecycler.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                getSharedShops();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getSharedShops() {
        if (ownerIds.size() > 0) {
            for (String ownerId : ownerIds) {
                myRef.child("Users").child(ownerId).child("Stores").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        sharedShopLists.clear();
                        if(dataSnapshot.getValue() != null) {
                            GenericTypeIndicator<ItemsList> t = new GenericTypeIndicator<ItemsList>() {};
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                ItemsList itemsList = singleSnapshot.getValue(t);
                                sharedShopLists.add(itemsList);
                            }
                        }
                        setShoppingRecycler(sharedShopLists);
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        Log.i("TAG", "onCancelled", databaseError.toException());
                    }
                });
            }
        }

    }


    @Override
    public void onItemClick(int position) {
        if ((pantryListMainRecycler.getVisibility() == View.VISIBLE) && (shoppingListMainRecycler.getVisibility() == View.GONE)) {
            myRef.child("Users").child(userId).child("SharedPantries").child(sharedPantryLists.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    getPantryInside(position, snapshot);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }
        else if((pantryListMainRecycler.getVisibility() == View.GONE) && (shoppingListMainRecycler.getVisibility() == View.VISIBLE)){
            Intent i = new Intent(this, ShoppingInside.class);
            i.putExtra("userPantryLists", sharedPantryLists);
            i.putExtra("shoppingListName", sharedShopLists.get(position).getName());
            i.putExtra("shoppingListId", sharedShopLists.get(position).getId());
            i.putExtra("OwnerId", owner);
            i.putExtra("EmailUser", userId);
            startActivity(i);
        }
    }

    public void getPantryInside(int position, DataSnapshot snapshot) {
        owner = snapshot.child("ownerId").getValue().toString();
        Intent i = new Intent(SharedPantriesShopsActivity.this, PantryInside.class);
        i.putExtra("pantryListName", sharedPantryLists.get(position).getName());
        i.putExtra("pantryListId", sharedPantryLists.get(position).getId());
        i.putExtra("OwnerId", owner);
        listPosition = position;
        startActivityForResult(i, 10030);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10030) {
            if (resultCode == RESULT_OK) {
                ArrayList<Item> itemsPantry = data.getParcelableArrayListExtra("returnedItemList");
                ItemsList pantry = sharedPantryLists.get(listPosition);
                pantry.setItemList(itemsPantry);
                pantryListRecyclerAdapter.notifyItemChanged(listPosition);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


}