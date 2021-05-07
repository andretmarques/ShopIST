package pt.ulisboa.tecnico.cmov.shopist;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Item implements Parcelable {


    private String name;
    //private int photoId;
    private int quantity = 0;
    private Double price = 0.0;
    private String id;
    private int toPurchase = 0;
    private String productBarcode = "No Barcode";
    private HashMap<String, String> shops = new HashMap<>();



    protected Item(Parcel in) {
        name = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
        id = in.readString();
        toPurchase = in.readInt();
        productBarcode = in.readString();
        shops = in.readHashMap(String.class.getClassLoader());
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

        return Objects.equals(name.toLowerCase().trim(), item.name.toLowerCase().trim());
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public HashMap<String, String> getShops() {
        return shops;
    }

    public void setShops(HashMap<String, String> shops) {
        this.shops = shops;
    }

    public void addShops(String id, String name){
        shops.put(id, name);
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Item(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getToPurchase() {
        return toPurchase;
    }

    public void setToPurchase(int toPurchase) {
        this.toPurchase = toPurchase;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public Item() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(quantity);
        dest.writeDouble(price);
        dest.writeString(id);
        dest.writeInt(toPurchase);
        dest.writeString(productBarcode);
        dest.writeMap(shops);

    }
}
