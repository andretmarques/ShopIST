package pt.ulisboa.tecnico.cmov.shopist;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Item implements Parcelable {


    private String name;
    //private int photoId;
    private int quantity = 0;
    private Double price = 0.0;
    private String id;
    private int toPurchase = 0;
    private int inCart;
    private String productBarcode = "No Barcode";
    private HashMap<String, String> shops = new HashMap<>();
    private HashMap<String, String> pantries = new HashMap<>();
    private HashMap<String, String> pantriesMap = new HashMap<>();
    private String imageEncoded;


    public Item(Parcel in) {
        name = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
        id = in.readString();
        toPurchase = in.readInt();
        productBarcode = in.readString();
        shops = in.readHashMap(String.class.getClassLoader());
        inCart = in.readInt();
        pantries = in.readHashMap(String.class.getClassLoader());
        pantriesMap = in.readHashMap(String.class.getClassLoader());
        imageEncoded = in.readString();
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

    public int getInCart() {
        return inCart;
    }

    public void setInCart(int inCart) {
        this.inCart = inCart;
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

    public HashMap<String, String> getPantries() {
        return pantries;
    }

    public void setPantries(HashMap<String, String> pantries) {
        this.pantries = pantries;
    }

    public HashMap<String, String> getPantriesMap() {
        return pantriesMap;
    }

    public void setPantriesMap(HashMap<String, String> pantriesMap) {
        this.pantriesMap = pantriesMap;
    }

    public String getImageEncoded() {
        return imageEncoded;
    }

    public void setImageEncoded(String imageEncoded) {
        this.imageEncoded = imageEncoded;
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
        dest.writeInt(inCart);
        dest.writeMap(pantries);
        dest.writeMap(pantriesMap);
        dest.writeString(imageEncoded);
    }
}
