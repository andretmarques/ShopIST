package pt.ulisboa.tecnico.cmov.shopist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ShoppingInside extends AppCompatActivity implements ItemRecyclerAdapter.OnItemListener {
    private final ArrayList<Item> itemsShop = new ArrayList<>();
    private ArrayList<ItemsList> allPantries = new ArrayList<>();
    private ItemsList cart;
    private String shopId;
    private TextView cartCount;
    private RecyclerView productsMainRecycler;
    private ImageButton cartButton;
    private ItemRecyclerAdapter itemRecyclerAdapter;
    private String uid;
    private String[] pantries;
    private HashMap<String, HashMap<Item, Integer>> productsPurchase = new HashMap<>();
    private String ownerId;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_shop);
        setSupportActionBar(findViewById(R.id.toolbar_shop));
        ActionBar actionBar = getSupportActionBar();

        TextView toolbarTitle = findViewById(R.id.toolbar_shop_title);
        cartCount = findViewById(R.id.count_cart);
        cartButton = findViewById(R.id.cart);

        cartButton.setVisibility(View.GONE);
        cartCount.setVisibility(View.GONE);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();


        Bundle b = getIntent().getExtras();
        if(b != null){
            allPantries = b.getParcelableArrayList("userPantryLists");
            ownerId = b.getString("OwnerId");
            uid = b.getString("EmailUser");
            String actionTitle = b.getString("shoppingListName");
            shopId = b.getString("shoppingListId");
            actionTitle = "Store: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);

        }


        cart = new ItemsList("Cart " + shopId, ItemsList.ListType.CART);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(cart.getItemList().size() > 0){
                    showConfirmDialog();
                }else {
                    finish();
                }
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        updateDataLocal();

    }

    private void setItemsRecycler(ArrayList<Item> products) {
        productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "S", this);
        itemRecyclerAdapter.setAddCartListener((view, position) -> {
          Item currentItem = itemsShop.get(position);
          showPickerDialog(currentItem, position);
        });
        cartCount.setVisibility(View.VISIBLE);
        cartButton.setVisibility(View.VISIBLE);
        cartCount.setText(String.valueOf(cart.getItemList().size()));
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    private void updateDataLocal(){
        for (ItemsList pantry : allPantries){
            for(Item i : pantry.getItemList()){
                if(i.getToPurchase() > 0 && i.getShops().containsKey(shopId)){
                    if (itemsShop.contains(i)) {
                        Item inList = itemsShop.get(itemsShop.indexOf(i));
                        inList.setToPurchase(inList.getToPurchase() + i.getToPurchase());
                        inList.getPantries().putAll(i.getPantries());
                        inList.getPantriesMap().putAll(i.getPantriesMap());

                    }else {
                        itemsShop.add(i);
                    }
                }
            }
        }
        setItemsRecycler(itemsShop);
    }

    private void populateList(Item currentItem){
        ArrayList<String> pantriesList= new ArrayList<>(currentItem.getPantries().keySet());

        pantries = pantriesList.toArray(new String[0]);
    }


    @Override
    public void onItemClick(int position) {
    }


    public void showPickerDialog(Item currentItem, int position) {
        populateList(currentItem);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View v = this.getLayoutInflater().inflate(R.layout.quantity_picker, null);
        builder.setView(v);
        builder.setTitle("How many " + currentItem.getName() + " you will buy?");
        final NumberPicker picker = v.findViewById(R.id.picker);
        final Spinner spinner = v.findViewById(R.id.drop_down_pantries);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pantries);
        spinner.setAdapter(adapter);
        picker.setMinValue(1);
        picker.setMaxValue(50);
        picker.setWrapSelectorWheel(false);
        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            String selectedPantryName = spinner.getSelectedItem().toString();
            String pantryId = currentItem.getPantries().get(selectedPantryName);
            int toBuy = Integer.parseInt(currentItem.getPantriesMap().get(pantryId));
            int pickerValue = picker.getValue();

            if(pickerValue >= toBuy){
                currentItem.setToPurchase(currentItem.getToPurchase() - toBuy);
                currentItem.getPantriesMap().put(pantryId, "0");
            }else {
                int updatedValue = currentItem.getToPurchase() - pickerValue;
                int updatedValue2 = toBuy - pickerValue;
                currentItem.setToPurchase(updatedValue);
                currentItem.getPantriesMap().put(pantryId, String.valueOf(updatedValue2));
            }

            int quantity = currentItem.getQuantity() + pickerValue;
            currentItem.setQuantity(quantity);

            if(productsPurchase.containsKey(pantryId)){
                if(productsPurchase.get(pantryId).containsKey(currentItem)) {
                    Integer purchased = productsPurchase.get(pantryId).get(currentItem) + pickerValue;
                    productsPurchase.get(pantryId).put(currentItem, purchased);
                }else{
                    productsPurchase.get(pantryId).put(currentItem, pickerValue);
                }
            }else {
                HashMap<Item, Integer> hm = new HashMap<>();
                hm.put(currentItem, pickerValue);

                productsPurchase.put(pantryId, hm);
            }



            if(cart.getItemList().contains(currentItem)){
                Item inCart = cart.getItemList().get(cart.getItemList().indexOf(currentItem));
                inCart.setInCart(inCart.getInCart() + picker.getValue());
                inCart.setQuantity(inCart.getQuantity() + picker.getValue());
            }else {
                currentItem.setInCart(picker.getValue());
                cart.getItemList().add(currentItem);
            }
            cartCount.setText(String.valueOf(cart.getItemList().size()));
            dialog.dismiss();
            itemRecyclerAdapter.notifyItemChanged(position);
            Snackbar snackbar = Snackbar.make(productsMainRecycler, "" + picker.getValue()
                    + " " + currentItem.getName() + " Added to cart!", Snackbar.LENGTH_SHORT);
            snackbar.show();

        }).setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    public void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("If you leave this page you will lose your cart");

        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> finish())
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    public void showCart(View view) {
            Intent i = new Intent(this, CartActivity.class);
            i.putExtra("cartList", cart.getItemList());
            i.putExtra("allPantries", allPantries);
            i.putExtra("UserId", uid);
            i.putExtra("fantasticHm", (Serializable) productsPurchase);
            i.putExtra("OwnerId", ownerId);
            startActivityForResult(i, 10078);
        }
}
