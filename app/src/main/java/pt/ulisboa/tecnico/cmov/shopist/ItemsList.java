package pt.ulisboa.tecnico.cmov.shopist;
import java.util.ArrayList;

public class ItemsList {
    public enum ListType {
        SHOP,
        PANTRY,
        CART;
    };
    ArrayList<Item> itemList;
    private ListType listType;
    private String name;
    private String location = "";

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

    public String getNumberItems(){
        return "" + itemList.size();
    }

    public ItemsList(String name, ListType type) {
        this.name = name;
        itemList = new ArrayList<Item>();
        this.listType = type;
    }
}