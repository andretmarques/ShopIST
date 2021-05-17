package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ncorti.slidetoact.SlideToActView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class CartActivity extends AppCompatActivity implements ItemRecyclerAdapter.OnItemListener {
    private RecyclerView productsMainRecycler;
    private ItemRecyclerAdapter itemRecyclerAdapter;
    private ArrayList<Item> itemsCart = new ArrayList<>();
    private ArrayList<Item> updatedItems = new ArrayList<>();
    private ArrayList<ItemsList> allPantries = new ArrayList<>();
    private DatabaseReference myRef;
    private String uid;
    private HashMap<String, HashMap<Item, Integer>> productsPurchase = new HashMap<>();
    boolean net;
    private String ownerId;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        net = isNetworkAvailable(this.getApplication());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();

        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsCart = b.getParcelableArrayList("cartList");
            allPantries = b.getParcelableArrayList("allPantries");
            ownerId = b.getString("OwnerId");
            if (ownerId != null) {
                uid = ownerId;
            } else uid = b.getString("UserId");
            userId = b.getString("UserId");
            productsPurchase = (HashMap<String, HashMap<Item, Integer>>) b.getSerializable("fantasticHm");
        }
        setItemsRecycler(itemsCart);
        setupEventCallbacks();
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "C", this);
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    private void setupEventCallbacks() {
        final SlideToActView slide = findViewById(R.id.slider_finish);
        slide.setOnSlideCompleteListener(view -> {
            String pantryName;
            for(HashMap.Entry<String, HashMap<Item, Integer>> entry : productsPurchase.entrySet()){
                for (HashMap.Entry<Item, Integer> secondEntry : entry.getValue().entrySet()){
                    pantryName = getPantryName(secondEntry.getKey(), entry.getKey());
                    updateDataBase(entry.getKey(), secondEntry.getKey(), secondEntry.getValue(), pantryName);
                }

            }


            itemsCart.clear();
            itemRecyclerAdapter.notifyDataSetChanged();
        });

        slide.setOnSlideToActAnimationEventListener(new SlideToActView.OnSlideToActAnimationEventListener() {

            @Override
            public void onSlideResetAnimationStarted(@NotNull SlideToActView slideToActView) {

            }

            @Override
            public void onSlideResetAnimationEnded(@NotNull SlideToActView slideToActView) {

            }

            @Override
            public void onSlideCompleteAnimationStarted(@NotNull SlideToActView slideToActView, float v) {

            }

            @Override
            public void onSlideCompleteAnimationEnded(@NonNull SlideToActView view) {
                Intent i = new Intent(CartActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("UserEmail", userId);
                setResult(CartActivity.RESULT_OK, i);
                finish();
                startActivity(i);

            }
    });
    }

    private String getPantryName(Item i, String pantryId){
        String pantryName = "";
        for(HashMap.Entry<String, String> hmhm : i.getPantries().entrySet()) {
            if (hmhm.getValue().equals(pantryId))
                pantryName = hmhm.getKey();
        }
        return pantryName;
    }

    private void updateDataBase(String pantryId, Item i, int purchased, String pantryName){
        myRef.child("Users").child(uid).child("Pantries").child(pantryId).addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int toBuy = Integer.parseInt(snapshot.child("toBuy").getValue().toString()) - purchased;
                if (toBuy < 0)
                    toBuy = 0;
                myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("toBuy").setValue(toBuy);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("name").getValue().toString().equals(i.getName())){

                        int toPurchase = Integer.parseInt(dataSnapshot.child("toPurchase").getValue().toString());
                        int quantity = Integer.parseInt(dataSnapshot.child("quantity").getValue().toString()) + purchased;

                        if(purchased >= toPurchase) {
                            toPurchase = 0;
                        }else{
                            toPurchase = toPurchase - purchased;
                        }
                        i.setToPurchase(toPurchase);
                        i.setQuantity(quantity);

                        if(i.getToPurchase() == 0){
                            i.getPantriesMap().remove(pantryId);
                            i.getPantries().remove(pantryName);
                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantries").removeValue();

                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantriesMap").removeValue();
                        }else {
                            i.getPantriesMap().put(pantryId, String.valueOf(i.getToPurchase()));
                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantries").setValue(i.getPantries());

                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantriesMap").setValue(i.getPantriesMap());
                        }

                        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                .child(dataSnapshot.getKey()).child("toPurchase").setValue(i.getToPurchase());

                        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                .child(dataSnapshot.getKey()).child("quantity").setValue(i.getQuantity());


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

    @Override
    public void onItemClick(int position) {

    }
}