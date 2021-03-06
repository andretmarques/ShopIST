package pt.ulisboa.tecnico.cmov.shopist;

import android.os.Parcel;
import android.os.Parcelable;

public class PublicItem implements Parcelable {
    private String barcode;
    private Double price;
    private String photoEncoded;

    public PublicItem(String barcode, Double price) {
        this.barcode = barcode;
        this.price = price;
    }

    protected PublicItem(Parcel in) {
        barcode = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        photoEncoded = in.readString();
    }


    public static final Creator<PublicItem> CREATOR = new Creator<PublicItem>() {
        @Override
        public PublicItem createFromParcel(Parcel in) {
            return new PublicItem(in);
        }

        @Override
        public PublicItem[] newArray(int size) {
            return new PublicItem[size];
        }
    };


    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPhotoEncoded() {
        return photoEncoded;
    }

    public void setPhotoEncoded(String photoEncoded) {
        this.photoEncoded = photoEncoded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(barcode);
        if (price == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(price);
        }
        parcel.writeString(photoEncoded);
    }
}
