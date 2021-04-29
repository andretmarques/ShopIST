package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CreateProductActivity extends AppCompatActivity {
    private TextView productName;
    private TextView productQuantity;
    private TextView productShop;
    //private Intent i;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_product_layout);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        productName = findViewById(R.id.product_name);
        productShop = findViewById(R.id.product_shop);
        productQuantity = findViewById(R.id.product_quantity);


    }


    public void cancelCreateProduct(View v) {
        finish();
    }

    public void createProductButton(View v) {
        if (productName.getText().toString().equals("")) {
            productName.setError("Name should not be empty");
        } else if (productQuantity.getText().toString().equals("") || productQuantity.getText().toString().trim().equals("0")) {
            productQuantity.setError("Quantity should not be empty or zero");
        } else if (productShop.getText().toString().equals("")) {
            productShop.setError("Shop should not be empty");
        } else {
            String productTextName = productName.getText().toString();
            String productShopName = productShop.getText().toString();
            int quantity = Integer.parseInt(productQuantity.getText().toString());
            Item newProduct = new Item(productTextName, productShopName);
            newProduct.setQuantity(quantity);
            newProduct.generateId();
            Intent intent = new Intent();
            intent.putExtra("returnedProduct", newProduct);
            setResult(CreateProductActivity.RESULT_OK, intent);
            finish();
        }
    }
}
