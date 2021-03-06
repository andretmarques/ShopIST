package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Item> itemsList;
    private final OnItemListener monItemListener;
    private final String type;
    private View.OnClickListener clickListener;
    private OnAddCartClick addCartListener;
    private OnConsumeClick consumeListener;


    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAddCartListener(OnAddCartClick  addCartListener) {
        this.addCartListener= addCartListener;
    }

    public void setConsumeListener(OnConsumeClick addConsumeListener) {
        this.consumeListener = addConsumeListener;
    }


    public ItemRecyclerAdapter(Context context, List<Item> itemsList, String type, OnItemListener onItemListener) {
        this.context = context;
        this.itemsList = itemsList;
        this.type = type;
        this.monItemListener = onItemListener;

    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (type) {
            case "P":
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.pantry_item_recycler_adapter, parent, false), monItemListener);
            case "S":
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.shopping_item_recycler_adapter, parent, false), monItemListener);
            case "C":
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.cart_item_recycler_adapter, parent, false), monItemListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item currentItem = itemsList.get(position);

        String itemName = "Name: " + currentItem.getName();
        String itemQuantity ="";

        switch (type) {
            case "P":
                itemQuantity = "Quantity available: " + currentItem.getQuantity();
                break;
            case "S":
                itemQuantity = "Quantity to buy: " + currentItem.getToPurchase();
                break;
            case "C":
                itemQuantity = "Quantity in cart: " + currentItem.getInCart();
                break;
        }


        holder.itemName.setText(itemName);
        if(holder.itemQuantity != null)
            holder.itemQuantity.setText(itemQuantity);

        if(holder.itemPrice != null) {
            String itemPrice = "Price: " + currentItem.getPrice() + "$";
            holder.itemPrice.setText(itemPrice);
        }
        if (clickListener!= null) {
            holder.itemView.setOnClickListener(clickListener);
        }

        if (addCartListener!= null) {
            holder.getAddCartButton().setOnClickListener(v -> addCartListener.onAddCart(v, position));
        }
        if(consumeListener !=null)
            holder.getConsumeButton().setOnClickListener(v -> consumeListener.onConsume(v, position));

        if (itemsList.get(position).getImageEncoded() != null) {
            String imageEncoded = itemsList.get(position).getImageEncoded();
            Bitmap bitmap = convertStringToBitmap(imageEncoded);
            holder.itemPhoto.setImageBitmap(bitmap);
        }

    }

    public void removeItem(int position){
        itemsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, itemsList.size());
    }

    public void restoreItem(Item deleted, int position){
        itemsList.add(position, deleted);
        notifyItemInserted(position);
    }

    public boolean haveItem(Item i){
        return itemsList.contains(i);
    }

    public void consumeQuantity(Item i, int p){
        itemsList.get(p).setQuantity(i.getQuantity()-1);
        itemsList.get(p).setToPurchase(i.getToPurchase()+1);
        notifyItemChanged(p);
    }

    public void addQuantity(Item i, int p){
        itemsList.get(p).setQuantity(i.getQuantity()+1);
        itemsList.get(p).setToPurchase(i.getToPurchase()-1);
        notifyItemChanged(p);
    }

    public void removeItemQuantity(Item i, int p){
        itemsList.get(p).setQuantity(i.getQuantity()-1);
        itemsList.remove(p);
        notifyItemRemoved(p);
        notifyItemRangeChanged(p, itemsList.size());
    }

    public int getItem(int p){
        return itemsList.get(p).getQuantity();
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public void setAddCartListener() {
    }

    public static final class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView itemName;
        TextView itemQuantity;
        TextView itemPrice;
        Button addToCart;
        Button consume;
        ImageView itemPhoto;
        OnItemListener onItemListener;

        public ItemViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemPrice = itemView.findViewById(R.id.item_price);
            addToCart = itemView.findViewById(R.id.add_to_cart);
            consume = itemView.findViewById(R.id.consume);
            itemPhoto = itemView.findViewById(R.id.item_image);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
            if(addToCart != null)
                addToCart.setOnClickListener(this);
            if(consume != null)
                consume.setOnClickListener(this);

            }
        public Button getAddCartButton() {
            return addToCart;

        }
        public Button getConsumeButton() {
            return consume;

        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition());

        }
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }

    public interface OnAddCartClick{
        void onAddCart(View view, int position);
    }

    public interface OnConsumeClick{
        void onConsume(View view, int position);
    }

    public static Bitmap convertStringToBitmap(String string) {
        byte[] byteArray1;
        byteArray1 = Base64.decode(string, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(byteArray1, 0, byteArray1.length);
    }




}


