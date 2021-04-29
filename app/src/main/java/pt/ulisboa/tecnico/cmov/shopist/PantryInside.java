package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PantryInside extends AppCompatActivity {
    private ArrayList<Item> itemsPantry = new ArrayList<>();
    private DatabaseReference myRef;
    private String pantryId;


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

        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsPantry = b.getParcelableArrayList("pantryProductsList");
            actionTitle = b.getString("pantryListName");
            actionTitle = "Pantry: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            pantryId = b.getString("pantryListId");
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }


        setItemsRecycler(itemsPantry);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("returnedItemList", itemsPantry);
                setResult(PantryInside.RESULT_OK, intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void setItemsRecycler(ArrayList<Item> products) {
        RecyclerView productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        ItemRecyclerAdapter itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "P");
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    public void createItem(View view){
        Intent intent = new Intent(this, CreateProductActivity.class);
        startActivityForResult(intent, 10015);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10015) {
            if (resultCode == RESULT_OK) {
                Item newItem = data.getParcelableExtra("returnedProduct");
                itemsPantry.add(newItem);
                myRef.child("Pantries").child(pantryId).child("itemList").setValue(itemsPantry);
                setItemsRecycler(itemsPantry);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}
