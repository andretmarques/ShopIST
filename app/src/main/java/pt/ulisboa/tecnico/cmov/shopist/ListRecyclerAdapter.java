package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerAdapter.MainViewHolder> {

    private final Context context;
    private final ArrayList<ItemsList> allLists;
    private final OnListListener mOnListListener;
    private ListRecyclerAdapter.OnClickGetDirections getDirectionsListener;
    private final String type;


    public void setDirectionsListener(ListRecyclerAdapter.OnClickGetDirections getDirectionsListener) {
        this.getDirectionsListener= getDirectionsListener;
    }

    public ListRecyclerAdapter(Context context, ArrayList<ItemsList> allLists, String type, OnListListener onListListener) {
        this.context = context;
        this.allLists = allLists;
        this.type = type;
        this.mOnListListener = onListListener;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (type.equals("PANTRY")){
            v = LayoutInflater.from(context).inflate(R.layout.pantry_recycler_row, parent, false);
            return new MainViewHolder(v, mOnListListener);
        }
        else if (type.equals("SHOP")) {
            v = LayoutInflater.from(context).inflate(R.layout.shop_recycler_row, parent, false);
            return new MainViewHolder(v, mOnListListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        if(allLists.get(position)!=null) {
            String numberItemsString = "Number of items: " + allLists.get(position).listSizeString();
            holder.listTitle.setText(allLists.get(position).getName());
            holder.itemCount.setText(numberItemsString);
            if(type.equals("SHOP")) {
                holder.listLocation.setText(allLists.get(position).getLocation());
                if(allLists.get(position).getEta() != null) {
                    holder.etaText.setText(allLists.get(position).getEta());
                }else {
                    holder.etaText.setText("");
                }

            }else if(type.equals("PANTRY")){
                String toBuyStr = "Items to buy: " + allLists.get(position).getToBuy();
                holder.toBuyCount.setText(toBuyStr);
            }

            if(allLists.get(position).getLocation() == null || allLists.get(position).getLocation().equals("")) {
                holder.getDirectionsButton().setVisibility(View.GONE);
            }

            if (getDirectionsListener!= null) {
                holder.getDirectionsButton().setOnClickListener(v -> getDirectionsListener.onGetDirections(v, position));
            }

        }

    }
    public void removeItem(int position){
        allLists.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, allLists.size());
    }

    public void restoreItem(ItemsList deleted, int position){
        allLists.add(position, deleted);
        notifyItemInserted(position);
    }




    @Override
    public int getItemCount() {
        return allLists.size();
    }

    public static final class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listTitle;
        TextView itemCount;
        TextView toBuyCount;
        TextView listLocation;
        TextView etaText;
        Button getDirections;
        OnListListener onListListener;


        public MainViewHolder(@NonNull View itemView, OnListListener onListListener) {
            super(itemView);

            listTitle = itemView.findViewById(R.id.list_title);
            itemCount = itemView.findViewById(R.id.item_numbers);
            toBuyCount = itemView.findViewById(R.id.items_to_buy);
            listLocation = itemView.findViewById(R.id.list_location);
            etaText = itemView.findViewById(R.id.eta);
            getDirections = itemView.findViewById(R.id.get_directions);
            this.onListListener = onListListener;
            itemView.setOnClickListener(this);


        }

        public Button getDirectionsButton() {
            return getDirections;

        }

        @Override
        public void onClick(View v) {
            onListListener.onItemClick(getAdapterPosition());

        }
    }

    public interface OnListListener {
        void onItemClick(int position);
    }

    public interface OnClickGetDirections{
        void onGetDirections(View view, int position);
    }

}
