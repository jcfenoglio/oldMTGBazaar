package edu.rosehulman.fenogljc.mtgbazaar.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Deck implements Parcelable {

    private String name;
    private String format;
    private String key;

    @SuppressWarnings("unused")
    public Deck() {
    }

    public Deck(String name, String format) {
        this.name = name;
        this.format = format;
    }

    protected Deck(Parcel in) {
        name = in.readString();
        format = in.readString();
        key = in.readString();
    }

    public static final Creator<Deck> CREATOR = new Creator<Deck>() {
        @Override
        public Deck createFromParcel(Parcel in) {
            return new Deck(in);
        }

        @Override
        public Deck[] newArray(int size) {
            return new Deck[size];
        }
    };

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(format);
        dest.writeString(key);
    }

    public void setValues(Deck updatedDeck) {
        setName(updatedDeck.getName());
        setFormat(updatedDeck.getFormat());
    }

}
