package pt.ulisboa.tecnico.cmov.shopist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PantryInside extends AppCompatActivity {
    private ArrayList<Item> itemsPantry = new ArrayList<>();
    private ArrayList<PublicItem> listPublic = new ArrayList<>();
    private DatabaseReference myRef;
    private String pantryId;
    Button scanBarcodeBtn;
    String barcode = "";
    Double price;
    String shop;
    ArrayList<String> shopList = new ArrayList<>();
    ArrayList<String> priceList = new ArrayList<>();
    String messageAll = "";

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

        scanBarcodeBtn = findViewById(R.id.scan_barcode);
        scanBarcodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(PantryInside.this, ScanBarcodeActivity.class), 10025);
            }
        });
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
        else if (requestCode == 10025) {
            if (resultCode == RESULT_OK) {
                barcode = data.getStringExtra("Barcode");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listPublic = (ArrayList<PublicItem>) snapshot.child("PublicItems").child(barcode).getValue();

                        if (!snapshot.child("PublicItems").child(barcode).exists()) {
                            ScanBarcodeAssist();
                        }
                        else {
                            if (listPublic.size() > 1) {
                                messageAll = "";
                                for (DataSnapshot singleSnapshot : snapshot.child("PublicItems").child(barcode).getChildren()) {
                                    Log.d("olaaa", singleSnapshot.toString());
                                    messageAll = messageAll + "Shop: " +
                                            singleSnapshot.child("shop").getValue().toString()
                                            + "\n" + "Price: " +
                                            singleSnapshot.child("price").getValue().toString() + "€"
                                            + "\n\n";

                                }
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PantryInside.this)
                                        .setTitle("Product " + barcode)
                                        .setMessage(messageAll)
                                        .setNegativeButton("Thank you", null)
                                        .setPositiveButton("Add new shop", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ScanBarcodeAssist();
                                            }
                                        });
                                alertDialogBuilder.show();
                                messageAll = "";
                            }
                            else {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PantryInside.this)
                                        .setTitle("Product " + barcode)
                                        .setMessage("Shop: " + snapshot.child("PublicItems").child(barcode).child("0").child("shop").getValue()
                                                + "\n" + "Price: " + snapshot.child("PublicItems").child(barcode).child("0").child("price").getValue() + "€")
                                        .setNegativeButton("Thank you", null)
                                        .setPositiveButton("Add new shop", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ScanBarcodeAssist();
                                            }
                                        });
                                alertDialogBuilder.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            return;
        }
        if (requestCode == 20221) {
            if (resultCode == RESULT_OK) {
                price = data.getDoubleExtra("price", 0);
                shop = data.getStringExtra("shop");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exists = false;
                        for (DataSnapshot singleSnapshot : snapshot.child("PublicItems").child(barcode).getChildren()) {
                            if (singleSnapshot.child("shop").getValue().toString().equals(shop)) {
                                Toast.makeText(getApplicationContext(), "This shop already has this item and price for it", Toast.LENGTH_LONG).show();
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            PublicItem newPublicItem = new PublicItem(barcode, price, shop);
                            Log.d("Achando", shop);
                            listPublic.add(newPublicItem);
                            myRef.child("PublicItems").child(barcode).setValue(listPublic);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void ScanBarcodeAssist() {
        Intent i = new Intent(PantryInside.this, PriceShopActivity.class);
        i.putExtra("barcode", barcode);
        startActivityForResult(i, 20221);
    }


}
