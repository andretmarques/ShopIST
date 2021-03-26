package pt.ulisboa.tecnico.cmov.shopist;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.shopist.Item;

public class ItemLists {
    private enum listType {
        SHOP,
        PANTRY,
        CART;
    };
    ArrayList<Item> itemList;
    private listType listType;
    private String name;


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

    public void setListType(ItemLists.listType listType) {
        this.listType = listType;
    }

    public ItemLists.listType getListType() {
        return listType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemLists(String name) {
        this.name = name;
    }
}