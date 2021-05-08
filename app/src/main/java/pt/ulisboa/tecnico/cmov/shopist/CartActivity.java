package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

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

public class CartActivity extends AppCompatActivity implements ItemRecyclerAdapter.OnItemListener {
    private RecyclerView productsMainRecycler;
    private ItemRecyclerAdapter itemRecyclerAdapter;
    private ArrayList<Item> itemsCart = new ArrayList<>();
    private ArrayList<ItemsList> allPantries = new ArrayList<>();
    private DatabaseReference myRef;
    private String uid;
    boolean net;


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
            uid = b.getString("UserId");
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
            if (net) {
                for(Item i : itemsCart){
                    for (String s : i.getPantries()){
                        updateData(s, i);
                    }
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
                i.putExtra("UserEmail", uid);
                startActivity(i);
            }
    });
    }

    private void updateData(String pantryId, Item item) {
        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(item.getName().equals(dataSnapshot.getValue(Item.class).getName())){
                        if(dataSnapshot.getValue(Item.class).getToPurchase() >= item.getInCart()){
                            int sub = dataSnapshot.getValue(Item.class).getToPurchase() - item.getInCart();
                            myRef.child("Users")
                                    .child(uid)
                                    .child("Pantries")
                                    .child(pantryId)
                                    .child("itemList")
                                    .child(dataSnapshot.getKey())
                                    .child("toPurchase").setValue(sub);

                            myRef.child("Users")
                                    .child(uid)
                                    .child("Pantries").child(pantryId)
                                    .child("itemList")
                                    .child(dataSnapshot.getKey())
                                    .child("quantity").setValue(item.getQuantity() + item.getInCart());
                        }else{
                            myRef.child("Users")
                                    .child(uid)
                                    .child("Pantries").child(pantryId)
                                    .child("itemList")
                                    .child(dataSnapshot.getKey())
                                    .child("quantity").setValue(item.getQuantity() + item.getInCart());

                            myRef.child("Users")
                                    .child(uid)
                                    .child("Pantries")
                                    .child(pantryId)
                                    .child("itemList")
                                    .child(dataSnapshot.getKey())
                                    .child("toPurchase").setValue(0);
                        }
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

    @Override
    public void onItemClick(int position) {

    }
}