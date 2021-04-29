package pt.ulisboa.tecnico.cmov.shopist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class Item implements Parcelable {
    protected Item(Parcel in) {
        name = in.readString();
        quantity = in.readInt();
        price = in.readInt();
        id = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(quantity);
        dest.writeInt(price);
        dest.writeString(id);
    }

    private String name;
    //private int id;
    //private int photoId;
    private int quantity = 0;
    private int price;
    private String id;
    private String shop;
    private final ArrayList<String> shops = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void generateId(){
        this.id = UUID.randomUUID().toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public ArrayList<String> getShops() {
        return shops;
    }

    public void addShops(String id){
        shops.add(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addQuantity(int q){
        this.quantity += q;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public Item(String name, int price, String shop) {
        this.name = name;
        this.price = price;
        this.shop = shop;
    }

    public Item() {
    }
}
