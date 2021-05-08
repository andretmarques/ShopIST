package pt.ulisboa.tecnico.cmov.shopist;

public class SharedPantry {
    private String pantryId;
    private String ownerId;



    public SharedPantry(String pantryId, String ownerId) {
        this.pantryId = pantryId;
        this.ownerId = ownerId;
    }

    public String getPantryId() {
        return pantryId;
    }

    public void setPantryId(String pantryId) {
        this.pantryId = pantryId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

}

