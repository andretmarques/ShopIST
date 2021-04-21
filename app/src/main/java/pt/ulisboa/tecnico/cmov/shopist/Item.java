package pt.ulisboa.tecnico.cmov.shopist;

public class Item {
    private String name;
    //private int id;
    private int photoId;
    private int quantity = 0;
    private int price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Item(String name, int quantity, int price) {
        this.name = name;
        this.quantity += quantity;
        this.price = price;
    }

    public Item() {
    }
}
