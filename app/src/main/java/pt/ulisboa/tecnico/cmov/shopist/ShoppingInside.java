package pt.ulisboa.tecnico.cmov.shopist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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
    private final HashMap<String, HashMap<Item, Integer>> productsPurchase = new HashMap<>();
    private String ownerId;
    private DatabaseReference myRef;
    private String cartPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_shop);
        setSupportActionBar(findViewById(R.id.toolbar_shop));
        ActionBar actionBar = getSupportActionBar();

        FirebaseDatabase database = FirebaseDatabase.getInstance("DATABASE_URL");
        myRef = database.getReference();

        TextView toolbarTitle = findViewById(R.id.toolbar_shop_title);
        cartCount = findViewById(R.id.count_cart);
        cartButton = findViewById(R.id.cart);

        cartButton.setVisibility(View.GONE);
        cartCount.setVisibility(View.GONE);


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
            getTotalPrice();
            Intent i = new Intent(this, CartActivity.class);
            i.putExtra("cartList", cart.getItemList());
            i.putExtra("UserId", uid);
            if (ownerId != null) {
            i.putExtra("OwnerId", ownerId);
            }
            i.putExtra("allPantries", allPantries);
            i.putExtra("cartPrice", cartPrice);
            startActivityForResult(i, 10078);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 10078){
            if(resultCode == RESULT_OK){
                data.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                uid = data.getStringExtra("UserEmail");
                allPantries = data.getParcelableArrayListExtra("allPantries");
                String pantryName;
                for(HashMap.Entry<String, HashMap<Item, Integer>> entry : productsPurchase.entrySet()){
                    for (HashMap.Entry<Item, Integer> secondEntry : entry.getValue().entrySet()){
                        pantryName = getPantryName(secondEntry.getKey(), entry.getKey());
                        updateDataBase(entry.getKey(), secondEntry.getKey(), secondEntry.getValue(), pantryName);

                    }

                }
                Intent i = new Intent();
                i.putParcelableArrayListExtra("allPantries", allPantries);

                setResult(ShoppingInside.RESULT_OK, i);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getTotalPrice() {
        double finalPrice = 0.0;
        for (Item i : cart.getItemList()) {
            finalPrice = finalPrice + i.getPrice()* (double) i.getInCart();
        }
        String value = String.valueOf(finalPrice);
        cartPrice = "Total price: " + value + "$";
    }

    private String getPantryName(Item i, String pantryId){
        String pantryName = "";
        for(HashMap.Entry<String, String> hmhm : i.getPantries().entrySet()) {
            if (hmhm.getValue().equals(pantryId))
                pantryName = hmhm.getKey();
        }
        return pantryName;
    }
    private void updateDataBase(String pantryId, Item i, int purchased, String pantryName){
        myRef.child("Users").child(uid).child("Pantries").child(pantryId).addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int toBuy = Integer.parseInt(snapshot.child("toBuy").getValue().toString()) - purchased;
                if (toBuy < 0)
                    toBuy = 0;
                myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("toBuy").setValue(toBuy);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("name").getValue().toString().equals(i.getName())){

                        int toPurchase = Integer.parseInt(dataSnapshot.child("toPurchase").getValue().toString());
                        int quantity = Integer.parseInt(dataSnapshot.child("quantity").getValue().toString()) + purchased;

                        if(purchased >= toPurchase) {
                            toPurchase = 0;
                        }else{
                            toPurchase = toPurchase - purchased;
                        }
                        i.setToPurchase(toPurchase);
                        i.setQuantity(quantity);

                        if(i.getToPurchase() == 0){
                            i.getPantriesMap().remove(pantryId);
                            i.getPantries().remove(pantryName);
                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantries").removeValue();

                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantriesMap").removeValue();
                        }else {
                            i.getPantriesMap().put(pantryId, String.valueOf(i.getToPurchase()));
                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantries").setValue(i.getPantries());

                            myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                    .child(dataSnapshot.getKey()).child("pantriesMap").setValue(i.getPantriesMap());
                        }

                        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                .child(dataSnapshot.getKey()).child("toPurchase").setValue(i.getToPurchase());

                        myRef.child("Users").child(uid).child("Pantries").child(pantryId).child("itemList")
                                .child(dataSnapshot.getKey()).child("quantity").setValue(i.getQuantity());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
