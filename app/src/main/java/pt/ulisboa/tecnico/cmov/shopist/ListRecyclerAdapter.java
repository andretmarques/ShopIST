package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerAdapter.MainViewHolder> {

    private final Context context;
    private final List<ItemsList> allLists;
    private final String type;

    public ListRecyclerAdapter(Context context, List<ItemsList> allLists, String type) {
        this.context = context;
        this.allLists = allLists;
        this.type = type;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (type.equals("SHOP")){
            return new MainViewHolder(LayoutInflater.from(context).inflate(R.layout.pantry_recycler_row, parent, false));
        }
        else
            return new MainViewHolder(LayoutInflater.from(context).inflate(R.layout.shop_recycler_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        if(allLists.get(position)!=null) {
            String numberItemsString = "Number of items: " + allLists.get(position).getNumberItems();
            holder.listTitle.setText(allLists.get(position).getName());
            holder.itemCount.setText(numberItemsString);
            holder.listLocation.setText(allLists.get(position).getLocation());
        }
//        setItemRecycler(holder.itemRecycler, allLists.get(position).getItemList());

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

    public static final class MainViewHolder extends RecyclerView.ViewHolder{

        TextView listTitle;
        TextView itemCount;
        TextView listLocation;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);

            listTitle = itemView.findViewById(R.id.list_title);
            itemCount = itemView.findViewById(R.id.item_numbers);
            listLocation = itemView.findViewById(R.id.list_location);

        }
    }

}
