package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        boolean net = isNetworkAvailable(this.getApplication());


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
        if (net){
            //updateData();
            setItemsRecycler(itemsPantry);
        }else {
            setItemsRecycler(itemsPantry);
        }
    }

    private void updateData() {
        myRef.child("Pantries").child(String.valueOf(pantryId)).child("itemList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<Item> t = new GenericTypeIndicator<Item>() {
                    };
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Item item = singleSnapshot.getValue(t);
                        itemsPantry.add(item);
                    }
                }
                setItemsRecycler(itemsPantry);
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
        RecyclerView productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        ItemRecyclerAdapter itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "P");
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
                Log.d("TAG", "onActivityResult: " + pantryId);
                myRef.child("Pantries").child(pantryId).child("itemList").setValue(itemsPantry);
                setItemsRecycler(itemsPantry);

            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
