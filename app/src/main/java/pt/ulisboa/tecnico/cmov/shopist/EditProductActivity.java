package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class EditProductActivity  extends AppCompatActivity {
    private Item item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product);
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_product_title);
        setSupportActionBar(findViewById(R.id.toolbar_product));
        ActionBar actionBar = getSupportActionBar();


        Intent i = getIntent();
        if(i != null){
            item = i.getParcelableExtra("product");
            actionTitle = "Product: " + item.getName();
            toolbarTitle.setText(actionTitle);
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    public void onClickCancel(View view){
        finish();
    }

    public void onClickConfirm(View view){
        TextView productName = findViewById(R.id.product_name);
        TextView quantityAval = findViewById(R.id.product_quantity);
        TextView quantityToBuy = findViewById(R.id.product_quantity_to_buy);
        String newName = productName.getText().toString();
        String newQuantity = quantityAval.getText().toString();
        String newTobuy = quantityToBuy.getText().toString();
        int available ;
        int toBuy;


        if(!newName.equals("")){
            item.setName(newName);
        }
        if(!newQuantity.equals("")){
            available = Integer.parseInt(newQuantity);
            item.setQuantity(available);
        }
        if(!newTobuy.equals("")){
            toBuy = Integer.parseInt(newTobuy);
            item.setToPurchase(toBuy);
        }

        Intent i = new Intent();
        i.putExtra("returnedItem", item);
        setResult(EditProductActivity.RESULT_OK, i);
        finish();
    }

}
