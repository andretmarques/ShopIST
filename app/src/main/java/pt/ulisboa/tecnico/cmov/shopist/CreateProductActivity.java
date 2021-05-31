package pt.ulisboa.tecnico.cmov.shopist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateProductActivity extends AppCompatActivity {
    private TextView productName;
    private TextView productQuantity;
    private final HashMap<String, String> shopsSelected = new HashMap<>();
    private HashMap<String, String> hashStoreNames = new HashMap<>();
    private ArrayList<String> storeKey;
    private String[] storeNames;
    private DatabaseReference myRef;
    private String userId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_product_layout);
        setSupportActionBar(findViewById(R.id.toolbar_main));
        FirebaseDatabase database = FirebaseDatabase.getInstance("DATABASE_URL");
        myRef = database.getReference();
        userId = getIntent().getStringExtra("EmailUser");

        productName = findViewById(R.id.product_name);
        productQuantity = findViewById(R.id.product_quantity);

        updateData();

    }

    public void chooseShop(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateProductActivity.this);
        builder.setTitle("Choose where you want to buy this item");
        ArrayList<Integer> shopsPosition = new ArrayList<>();

        builder.setMultiChoiceItems(storeNames, null, (dialog, which, isChecked) -> {
            if (isChecked) {
                shopsPosition.add(which);
            } else if (shopsPosition.contains(which)) {
                shopsPosition.remove(Integer.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            for (Integer i : shopsPosition)
                shopsSelected.put(storeKey.get(i), storeNames[i]);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateData() {
        myRef.child("Users").child(userId).child("StoreNames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                    hashStoreNames = dataSnapshot.getValue(t);
                    assert hashStoreNames != null;
                    ArrayList<String> storeNamesArray = new ArrayList<>(hashStoreNames.values());
                    storeKey = new ArrayList<>(hashStoreNames.keySet());
                    storeNames = storeNamesArray.toArray(new String[0]);
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.i("TAG", "onCancelled", databaseError.toException());
            }
        });
    }

        public void cancelCreateProduct(View v) {
        finish();
    }

    public void createProductButton(View v) {
        if (productName.getText().toString().equals("")) {
            productName.setError("Name should not be empty");
        } else if (productQuantity.getText().toString().equals("") || productQuantity.getText().toString().trim().equals("0")) {
            productQuantity.setError("Quantity should not be empty or zero");
        } else {
            String productTextName = productName.getText().toString();
            int quantity = Integer.parseInt(productQuantity.getText().toString());
            Item newProduct = new Item(productTextName);
            //newProduct.setShops(shopsSelected);
            newProduct.setQuantity(quantity);
            newProduct.generateId();
            newProduct.setShops(shopsSelected);
            Intent intent = new Intent();
            intent.putExtra("returnedProduct", newProduct);
            setResult(CreateProductActivity.RESULT_OK, intent);
            finish();
        }
    }
}
