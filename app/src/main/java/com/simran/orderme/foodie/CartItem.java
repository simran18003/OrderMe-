package com.simran.orderme.foodie;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String foodName;
    private String foodPrice;
    private int quantity;

    // Default constructor required for calls to DataSnapshot.getValue(CartItem.class)
    public CartItem() {
        // No-argument constructor
    }

    public CartItem(String foodName, String foodPrice, int quantity) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.quantity = quantity;
    }

    protected CartItem(Parcel in) {
        foodName = in.readString();
        foodPrice = in.readString();
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(foodName);
        dest.writeString(foodPrice);
        dest.writeInt(quantity);
    }
}
