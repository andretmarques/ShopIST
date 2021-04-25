package pt.ulisboa.tecnico.cmov.shopist;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ShoppingInside extends AppCompatActivity {
    private ArrayList<Item> itemsShop = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_shop);
        setSupportActionBar(findViewById(R.id.toolbar_shop));
        ActionBar actionBar = getSupportActionBar();
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_shop_title);






        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsShop = b.getParcelableArrayList("shoppingProductsList");
            actionTitle = b.getString("shoppingListName");
            actionTitle = "Store: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setItemsRecycler(itemsShop);
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        RecyclerView productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        ItemRecyclerAdapter itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "S");
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }



}
