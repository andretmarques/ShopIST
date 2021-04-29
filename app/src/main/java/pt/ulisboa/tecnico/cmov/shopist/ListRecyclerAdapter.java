package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerAdapter.MainViewHolder> {

    private final Context context;
    private final ArrayList<ItemsList> allLists;
    private final OnListListener mOnListListener;
    private final String type;

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
            String numberItemsString = "Number of items: " + allLists.get(position).getNumberItems();
            holder.listTitle.setText(allLists.get(position).getName());
            holder.itemCount.setText(numberItemsString);
            holder.listLocation.setText(allLists.get(position).getLocation());
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
        TextView listLocation;
        OnListListener onListListener;


        public MainViewHolder(@NonNull View itemView, OnListListener onListListener) {
            super(itemView);

            listTitle = itemView.findViewById(R.id.list_title);
            itemCount = itemView.findViewById(R.id.item_numbers);
            listLocation = itemView.findViewById(R.id.list_location);
            this.onListListener = onListListener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onListListener.onItemClick(getAdapterPosition());

        }
    }

    public interface OnListListener {
        void onItemClick(int position);
    }

}
