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

    public ListRecyclerAdapter(Context context, List<ItemsList> allLists) {
        this.context = context;
        this.allLists = allLists;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MainViewHolder(LayoutInflater.from(context).inflate(R.layout.list_recycler_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        String numberItemsString = "Number of items: " + allLists.get(position).getNumberItems();
        holder.listTitle.setText(allLists.get(position).getName());
        holder.itemCount.setText(numberItemsString);
        holder.listLocation.setText(allLists.get(position).getLocation());
//        setItemRecycler(holder.itemRecycler, allLists.get(position).getItemList());

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
