package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditProductActivity  extends AppCompatActivity {
    private Item item;
    TextView productName;
    TextView quantityAval;
    TextView quantityToBuy;
    EditText price;
    TextView barcodeView;
    String barcode;
    private DatabaseReference myRef;
    private ArrayList<PublicItem> listPublic = new ArrayList<>();
    String userId;
    String pantryId;
    boolean repeated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_product_title);
        setSupportActionBar(findViewById(R.id.toolbar_product));
        ActionBar actionBar = getSupportActionBar();

        productName = findViewById(R.id.product_name);
        quantityAval = findViewById(R.id.product_quantity);
        quantityToBuy = findViewById(R.id.product_quantity_to_buy);
        price = findViewById(R.id.product_price);
        barcodeView = findViewById(R.id.product_barcode_text);
        userId = getIntent().getStringExtra("UserId");
        pantryId = getIntent().getStringExtra("PantryId");

        Intent i = getIntent();
        if(i != null){
            item = i.getParcelableExtra("product");
            actionTitle = "Product: " + item.getName();
            toolbarTitle.setText(actionTitle);
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (item.getPrice() != 0.0) {
            price.setText(String.valueOf(item.getPrice()));
        }

        if (item.getProductBarcode().equals("No Barcode")) {
            barcodeView.setOnClickListener(view -> startActivityForResult(new Intent(EditProductActivity.this, ScanBarcodeActivity.class), 20025));
        } else {
            barcodeView.setText(item.getProductBarcode());
            barcodeView.setTypeface(null, Typeface.BOLD);
        }

    }

    public void onClickCancel(View view){
        finish();
    }

    public void onClickConfirm(View view){
        String newName = productName.getText().toString();
        String newQuantity = quantityAval.getText().toString();
        String newTobuy = quantityToBuy.getText().toString();
        String newPrice = price.getText().toString();
        String newBarcode = barcodeView.getText().toString();
        int available ;
        int toBuy;
        double doublePrice;


        if(!newName.equals("")) {
            item.setName(newName);
        }
        if(!newQuantity.equals("")) {
            available = Integer.parseInt(newQuantity);
            item.setQuantity(available);
        }
        if(!newTobuy.equals("")) {
            toBuy = Integer.parseInt(newTobuy);
            item.setToPurchase(toBuy);
            item.getPantriesMap().put(pantryId, String.valueOf(toBuy));
        }
        if(!newPrice.equals("")) {
            doublePrice = Double.parseDouble(newPrice);
            doublePrice = Math.floor(doublePrice * 100) / 100;

            // This warning is STUPID because it prevents from constant DB update when the price remains the same
            if ((!item.getProductBarcode().equals("No Barcode") || barcode != null) && !newPrice.equals(item.getPrice())) {
                myRef.child("PublicItems").child(barcode).setValue(new PublicItem(barcode, doublePrice));
            } else {
                item.setPrice(doublePrice);
            }

        }
        if(newBarcode.equals(barcode)) {
            item.setProductBarcode(newBarcode);
        }

        Intent i = new Intent();
        i.putExtra("returnedItem", item);
        setResult(EditProductActivity.RESULT_OK, i);
        finish();
    }

    private void userHasRepeatedBarcode() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("scan", pantryId);
                for (DataSnapshot singleSnapshot : snapshot.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").getChildren()) {
                    if (singleSnapshot.child("productBarcode").getValue().toString().equals(barcode)) {
                        Log.d("repeated", singleSnapshot.child("productBarcode").getValue().toString());
                        Toast.makeText(EditProductActivity.this, "You already have an item with this barcode", Toast.LENGTH_LONG).show();
                        repeated = true;
                        finish();
                        break;
                    }
                }
                if (!repeated) {
                    scanCrowdSourceBarcode();
                }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void scanCrowdSourceBarcode() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("PublicItems").child(barcode).exists()) {
                    Toast.makeText(EditProductActivity.this, "Product has already been shared. Price has been written", Toast.LENGTH_LONG).show();
                    Double varPrice = Double.parseDouble(snapshot.child("PublicItems").child(barcode).child("price").getValue().toString());
                    item.setPrice(varPrice);
                    price.setText(String.valueOf(varPrice));
                    price.setFocusable(true);
                    price.requestFocus();
                    barcodeView.setText(barcode);
                    barcodeView.setTypeface(null, Typeface.BOLD);
                } else {
                    Toast.makeText(EditProductActivity.this, "Write a Price and press confirm to share product info", Toast.LENGTH_LONG).show();
                    barcodeView.setText(barcode);
                    barcodeView.setTypeface(null, Typeface.BOLD);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 20025) {
            if (resultCode == RESULT_OK) {
                barcode = data.getStringExtra("Barcode");
                Log.d("barcode", barcode);
                userHasRepeatedBarcode();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }




}
