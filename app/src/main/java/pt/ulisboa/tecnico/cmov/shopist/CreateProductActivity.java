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
    private TextView productPrice;
    private TextView productShop;
    //private Intent i;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_product_layout);
        setSupportActionBar(findViewById(R.id.toolbar_main));

        productName = findViewById(R.id.product_name);
        productShop = findViewById(R.id.product_shop);
        productPrice = findViewById(R.id.product_price);


    }


    public void cancelCreateProduct(View v) {
        finish();
    }

    public void createProductButton(View v) {
        if (productName.getText().toString().equals("")) {
            productName.setError("Name should not be empty");
        } else if (productPrice.getText().toString().equals("")) {
            productPrice.setError("Name should not be empty");
        } else if (productShop.getText().toString().equals("")) {
            productShop.setError("Name should not be empty");
        } else {
            String productTextName = productName.getText().toString();
            String productShopName = productShop.getText().toString();
            int price = Integer.parseInt(productPrice.getText().toString());
            Item newProduct = new Item(productTextName, price, productShopName);
            newProduct.generateId();
            Log.d("TAG", "createProductButton: " + newProduct.getId());
            Intent intent = new Intent();
            intent.putExtra("returnedProduct", newProduct);
            setResult(CreateProductActivity.RESULT_OK, intent);
            finish();
        }
    }
}
