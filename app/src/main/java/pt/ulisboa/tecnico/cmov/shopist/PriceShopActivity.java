package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PriceShopActivity extends AppCompatActivity {
    private EditText priceText;
    private EditText shopText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_shop);

        priceText = findViewById(R.id.editTextPrice);
        shopText = findViewById(R.id.editTextShop);
        Button cancel = findViewById(R.id.cancel_price_shop);
        Button confirm = findViewById(R.id.confirm_price_shop);
        TextView barcodeView = findViewById(R.id.product_barcode_text);
        Bundle b = getIntent().getExtras();
        String barcode = b.getString("barcode");

        barcodeView.setText(barcode);

        cancel.setOnClickListener(view -> finish());

        confirm.setOnClickListener(view -> confirmButton());
    }

    public void confirmButton() {
        if (!priceText.getText().toString().equals("") && !shopText.getText().toString().equals("")
                && priceText.getText().toString().matches("[0-9.]*")) {
            Double itemPrice = Double.parseDouble(priceText.getText().toString());
            itemPrice = Math.floor(itemPrice * 100) / 100;
            String itemShop = shopText.getText().toString();
            Intent i = new Intent();
            i.putExtra("price", itemPrice);
            i.putExtra("shop", itemShop);
            setResult(PriceShopActivity.RESULT_OK, i);
            finish();
        }
        else if (priceText.getText().toString().equals("") && !shopText.getText().toString().equals("")) {
            priceText.setError("Price must not be empty");
        }
        else if (!priceText.getText().toString().equals("") && shopText.getText().toString().equals("")) {
            shopText.setError("Shop must not be empty");
        }
        else if (!priceText.getText().toString().matches("[0-9.]*")) {
            priceText.setError("Price must be numbers");
        }
        else {
            priceText.setError("Empty");
            shopText.setError("Empty");
        }

    }
}
