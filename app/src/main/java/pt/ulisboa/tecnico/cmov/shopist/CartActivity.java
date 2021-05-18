package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
    private DatabaseReference myRef;
    private String uid;
    private ArrayList<ItemsList> allPantries = new ArrayList<>();
    boolean net;
    private String ownerId;
    private TextView totalPrice;
    String cartPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        net = isNetworkAvailable(this.getApplication());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        totalPrice = findViewById(R.id.total_price);


        Bundle b = getIntent().getExtras();
        if(b != null){
            allPantries = b.getParcelableArrayList("allPantries");
            itemsCart = b.getParcelableArrayList("cartList");
            cartPrice = b.getString("cartPrice");
            ownerId = b.getString("OwnerId");
            if (ownerId != null) {
                uid = ownerId;
            } else uid = b.getString("UserId");
        }

        setItemsRecycler(itemsCart);
        setupEventCallbacks();
        totalPrice.setText(cartPrice);
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

                Intent i = new Intent();
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("UserEmail", uid);
                i.putParcelableArrayListExtra("allPantries", allPantries);
                setResult(CartActivity.RESULT_OK, i);
                finish();

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