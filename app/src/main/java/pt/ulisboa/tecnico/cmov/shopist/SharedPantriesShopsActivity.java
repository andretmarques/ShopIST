package pt.ulisboa.tecnico.cmov.shopist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SharedPantriesShopsActivity extends AppCompatActivity implements ListRecyclerAdapter.OnListListener {

    private ArrayList<ItemsList> sharedPantryLists = new ArrayList<>();
    private DatabaseReference myRef;
    private String userId;
    private RecyclerView pantryListMainRecycler;
    private RecyclerView shoppingListMainRecycler;
    private ListRecyclerAdapter pantryListRecyclerAdapter;
    private ListRecyclerAdapter shoppingListRecyclerAdapter;
    private Button sharedPantriesBtn;
    private Button sharedShopsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_pantries_shops);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();

        userId = getIntent().getStringExtra("UserEmail");
        sharedPantryLists = getIntent().getParcelableArrayListExtra("sharedPantries");

        pantryListMainRecycler = findViewById(R.id.pantry_recycler);
        shoppingListMainRecycler = findViewById(R.id.shopping_recycler);
        sharedPantriesBtn = findViewById(R.id.shared_pantries);
        sharedShopsBtn = findViewById(R.id.shared_stores);

        shoppingListMainRecycler.setVisibility(View.GONE);
        pantryListMainRecycler.setVisibility(View.VISIBLE);

        setPantryRecycler(sharedPantryLists);
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
        shoppingListRecyclerAdapter = new ListRecyclerAdapter(this, allLists, "SHOP", this);
        shoppingListMainRecycler.setAdapter(shoppingListRecyclerAdapter);
    }


    @Override
    public void onItemClick(int position) {


    }
}