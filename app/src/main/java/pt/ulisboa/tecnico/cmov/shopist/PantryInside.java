package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PantryInside extends AppCompatActivity {
    private ArrayList<Item> itemsPantry = new ArrayList<>();
    private ItemRecyclerAdapter itemRecyclerAdapter;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_pantry);
        setSupportActionBar(findViewById(R.id.toolbar_pantry));
        ActionBar actionBar = getSupportActionBar();
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_pantry_title);


        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsPantry = b.getParcelableArrayList("pantryProductsList");
            actionTitle = b.getString("pantryListName");
            actionTitle = "Pantry: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setItemsRecycler(itemsPantry);
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        RecyclerView productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "P");
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    public void manageItems(View view) {
        Intent i = new Intent(this, ManageItemsActivity.class);
        i.putParcelableArrayListExtra("pantryItems", itemsPantry);
        startActivityForResult(i, 10011);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10011) {
            if (resultCode == RESULT_OK) {
                itemsPantry = data.getParcelableArrayListExtra("returnedList");
                //itemRecyclerAdapter.notifyDataSetChanged();
                setItemsRecycler(itemsPantry);


            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
