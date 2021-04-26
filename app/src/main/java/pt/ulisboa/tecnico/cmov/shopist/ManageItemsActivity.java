package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ManageItemsActivity extends AppCompatActivity{
    private ArrayList<Item> itemsPantry = new ArrayList<>();
    private final ArrayList<Item> allItems = new ArrayList<>();
    RecyclerView productsMainRecycler;
    private final String quantity = "1";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_items);
        setSupportActionBar(findViewById(R.id.toolbar_allItems));
        ActionBar actionBar = getSupportActionBar();
        Item pao3 = new Item("Pregos", 10);
        pao3.generateId();
        allItems.add(pao3);
        //allItems.add(itemsPantry.get(0));
        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsPantry = b.getParcelableArrayList("pantryItems");
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setItemsRecycler(allItems);
        enableSwipeProduct();
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        ItemRecyclerAdapter itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "ALL");
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    private void enableSwipeProduct() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.RIGHT) {
                    final Item swipedItem = allItems.get(position);

                    if(itemsPantry.contains(swipedItem)){
                        int index = itemsPantry.indexOf(swipedItem);
                        Item toChange = itemsPantry.get(index);
                        toChange.addQuantity(Integer.parseInt(quantity));
                    }else {
                        swipedItem.setQuantity(Integer.parseInt(quantity));
                        itemsPantry.add(swipedItem);
                    }

                    Snackbar snackbar = Snackbar.make(productsMainRecycler, "List " + swipedItem.getName() + " Added", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();
                Drawable drawable = ContextCompat.getDrawable(ManageItemsActivity.this,R.drawable.add_swipe_layout);
                final ColorDrawable background = new ColorDrawable(Color.parseColor("#12752c"));
                assert drawable != null;
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + 20, itemView.getBottom());

                int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int iconLeft = (itemHeight - intrinsicHeight) / 2;
                int iconRight = iconLeft + intrinsicWidth;
                int iconBottom = iconTop + intrinsicHeight;
                drawable.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.draw(c);
                drawable.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(productsMainRecycler);
    }

}
