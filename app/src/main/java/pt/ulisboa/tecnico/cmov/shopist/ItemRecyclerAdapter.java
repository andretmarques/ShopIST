package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Item> itemsList;
    private final String type;

    public ItemRecyclerAdapter(Context context, List<Item> itemsList, String type) {
        this.context = context;
        this.itemsList = itemsList;
        this.type = type;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (type) {
            case "P":
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.pantry_item_recycler_adapter, parent, false));
            case "S":
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.shopping_item_recycler_adapter, parent, false));
            case "ALL":
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.all_item_recycler_adapter, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item currentItem = itemsList.get(position);

        String itemName = "Name: " + currentItem.getName();
        String itemQuantity ="";

        if(type.equals("P")) {
            itemQuantity = "Quantity available: " + currentItem.getQuantity();
        }
        else if(type.equals("S")){
            itemQuantity = "Quantity to buy: " + currentItem.getQuantity();
        }


        holder.itemName.setText(itemName);
        if(holder.itemQuantity != null)
            holder.itemQuantity.setText(itemQuantity);

        if(holder.itemPrice != null) {
            String itemPrice = "Price: " + currentItem.getPrice() + "$";
            holder.itemPrice.setText(itemPrice);
        }
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public static final class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView itemName;
        TextView itemQuantity;
        TextView itemPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemPrice = itemView.findViewById(R.id.item_price);

            if((itemView.findViewById(R.id.consume) != null ) &&  itemView.findViewById(R.id.add) != null) {

                itemView.findViewById(R.id.consume).setOnClickListener(v -> {


                });

                itemView.findViewById(R.id.add_more).setOnClickListener(v -> {

                });
            }

        }


    }
}

