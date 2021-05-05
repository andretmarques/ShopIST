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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShoppingInside extends AppCompatActivity implements ItemRecyclerAdapter.OnItemListener {
    private final ArrayList<Item> itemsShop = new ArrayList<>();
    private ArrayList<ItemsList> allPantries = new ArrayList<>();
    private ArrayList<String> allProducts = new ArrayList<>();
    private String shopId;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_shop);
        setSupportActionBar(findViewById(R.id.toolbar_shop));
        ActionBar actionBar = getSupportActionBar();

        TextView toolbarTitle = findViewById(R.id.toolbar_shop_title);


        Bundle b = getIntent().getExtras();
        if(b != null){
            allPantries = b.getParcelableArrayList("userPantryLists");
            String actionTitle = b.getString("shoppingListName");
            shopId = b.getString("shoppingListId");
            actionTitle = "Store: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }
        updateDataLocal();
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        RecyclerView productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        ItemRecyclerAdapter itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "S", this);
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    private void updateDataLocal(){
        for (ItemsList pantry : allPantries){
            for(Item i : pantry.getItemList()){
                if(i.getToPurchase() > 0 && i.getShops().containsKey(shopId)){
                    if (itemsShop.contains(i)) {
                        Item inList = itemsShop.get(itemsShop.indexOf(i));
                        inList.setToPurchase(inList.getToPurchase() + i.getToPurchase());

                    }else {
                        itemsShop.add(i);
                    }
                }
            }
        }
        setItemsRecycler(itemsShop);
    }
    @Override
    public void onItemClick(int position) {
    }
}
