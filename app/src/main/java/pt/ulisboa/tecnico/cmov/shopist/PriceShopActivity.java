package pt.ulisboa.tecnico.cmov.shopist;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PriceShopActivity extends AppCompatActivity {
    EditText priceText;
    EditText shopText;
    Button cancel;
    Button confirm;
    TextView barcodeView;
    String barcode;
    Double itemPrice;
    String itemShop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_shop);

        priceText = findViewById(R.id.editTextPrice);
        shopText = findViewById(R.id.editTextShop);
        cancel = findViewById(R.id.cancel_price_shop);
        confirm = findViewById(R.id.confirm_price_shop);
        barcodeView = findViewById(R.id.product_barcode_text);
        Bundle b = getIntent().getExtras();
        barcode = b.getString("barcode");

        barcodeView.setText(barcode);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmButton();
            }
        });
    }

    public void confirmButton() {
        if (!priceText.getText().toString().equals("") && !shopText.getText().toString().equals("")
                && priceText.getText().toString().matches("[0-9.]*")) {
            itemPrice = Double.parseDouble(priceText.getText().toString());
            itemPrice = Math.floor(itemPrice * 100) / 100;
            itemShop = shopText.getText().toString();
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
