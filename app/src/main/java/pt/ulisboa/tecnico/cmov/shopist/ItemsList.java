package pt.ulisboa.tecnico.cmov.shopist;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.UUID;

public class ItemsList implements Parcelable {
    protected ItemsList(Parcel in) {
        name = in.readString();
        location = in.readString();
        itemList = in.readArrayList(Item.class.getClassLoader());
        toBuy = in.readInt();
        id = in.readString();
        eta = in.readString();
    }

    public static final Creator<ItemsList> CREATOR = new Creator<ItemsList>() {
        @Override
        public ItemsList createFromParcel(Parcel in) {
            return new ItemsList(in);
        }

        @Override
        public ItemsList[] newArray(int size) {
            return new ItemsList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(location);
        dest.writeList(itemList);
        dest.writeInt(toBuy);
        dest.writeString(id);
        dest.writeString(eta);
    }

    public enum ListType {
        SHOP,
        PANTRY,
        CART
    }
    ArrayList<Item> itemList = new ArrayList<>();
    private ListType listType;
    private String name;
    private String location = "";
    private String id;
    private String eta;
    private int toBuy;

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public int getToBuy() {
        return toBuy;
    }

    public void setToBuy(int toBuy) {
        this.toBuy = toBuy;
    }

    public String getId() {
        return id;
    }

    public void generateId(){
        this.id = UUID.randomUUID().toString();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<Item> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Item> itemList) {
        this.itemList = itemList;
    }

    public void addToList(Item item){
        itemList.add(item);
    }

    public void removeFromList(Item item){
        itemList.remove(item);
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public ListType getListType() {
        return listType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String listSizeString(){
        return "" + itemList.size();
    }

    public ItemsList(String name, ListType type) {
        this.name = name;
        this.listType = type;
    }

    public ItemsList() {
    }
}