package edu.rosehulman.fenogljc.mtgbazaar.models;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import edu.rosehulman.fenogljc.mtgbazaar.Callback;
import edu.rosehulman.fenogljc.mtgbazaar.Constants;
import edu.rosehulman.fenogljc.mtgbazaar.adapters.TradeAdapter.TradeCallback;

public class UserCard implements Serializable, Parcelable {

    //TODO: add condition functionality
    private Card card;
    private String set;
    private int qty = 1;
    private float price = 0.0f;
    private String language = Constants.LANG_EN;
    private boolean foil = false;
    private String condition = Constants.COND_ARRAY[0];
    private String key;
    private String name;
    private Callback mPriceCallback;

    public UserCard () {}

    public UserCard(String cardName) {
        setName(cardName);
    }

    public UserCard(Card card) {
        setCard(card);
    }

    protected UserCard(Parcel in) {
        set = in.readString();
        qty = in.readInt();
        price = in.readFloat();
        language = in.readString();
        foil = in.readByte() != 0;
        condition = in.readString();
        key = in.readString();
        name = in.readString();
    }

    public static final Creator<UserCard> CREATOR = new Creator<UserCard>() {
        @Override
        public UserCard createFromParcel(Parcel in) {
            return new UserCard(in);
        }

        @Override
        public UserCard[] newArray(int size) {
            return new UserCard[size];
        }
    };

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public int getQty() { return qty; }

    public void setQty(int qty) { this.qty = qty; }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isFoil() {
        return foil;
    }

    public void setFoil(boolean foil) {
        this.foil = foil;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void setValues(UserCard newUserCard) {
        setSet(newUserCard.getSet());
        setQty(newUserCard.getQty());
        setPrice(newUserCard.getPrice());
        setLanguage(newUserCard.getLanguage());
        setFoil(newUserCard.isFoil());
        setCondition(newUserCard.getCondition());
    }

    public void setCardFromName(final Callback callback) {
        if(!name.equals("")) {
            final UserCard userCard = this;
            FirebaseDatabase.getInstance().getReference().child(Constants.DB_CARDS_REF).child(userCard.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Card incomingCard = dataSnapshot.getValue(Card.class);
                    incomingCard.setKey(dataSnapshot.getKey());
                    userCard.setCard(incomingCard);
                    if (userCard.getSet() == null) {
                        userCard.setSet(userCard.getCard().getSets().get(0));
                    }
                    Log.d(Constants.TAG, "onDataChange: " + getName());
                    callback.onCardFound(userCard);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void setCardFromName(final TradeCallback callback, final String side) {
        if(!name.equals("")) {
            final UserCard userCard = this;
            FirebaseDatabase.getInstance().getReference().child(Constants.DB_CARDS_REF).child(userCard.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Card incomingCard = dataSnapshot.getValue(Card.class);
                    incomingCard.setKey(dataSnapshot.getKey());
                    userCard.setCard(incomingCard);
                    if (userCard.getSet() == null) {
                        userCard.setSet(userCard.getCard().getSets().get(0));
                    }
                    Log.d(Constants.TAG, "onDataChange: " + getName());
                    callback.onCardFound(userCard, side);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriceFromInfo(Callback callback) {
        int position = card.getSets().indexOf(set);
        mPriceCallback = callback;
        (new GetPriceClass()).execute("http://api.tcgplayer.com/pricing/product/" + card.getTcgplayerid().get(position));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(set);
        dest.writeInt(qty);
        dest.writeFloat(price);
        dest.writeString(language);
        dest.writeByte((byte) (foil ? 1 : 0));
        dest.writeString(condition);
        dest.writeString(key);
        dest.writeString(name);
    }

    private class GetPriceClass extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urlStrings) {
            // http://api.tcgplayer.com/pricing/product/
            String urlString = urlStrings[0];
            Log.d("PICS", "doInBackground: " + urlString);
            URL url;
            JSONObject json = null;
            try {
                // Fetch the card info from TCGPlayer
                url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "bearer " + Constants.TCGPLAYER_BEARER_TOKEN);
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                json = new JSONObject(jsonText);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }

        private String readAll(BufferedReader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(JSONObject j) {
            try {
                for( int x = 0; x < j.getJSONArray("results").length(); x++) {
                    JSONObject jsonObject = j.getJSONArray("results").getJSONObject(x);
                    if (jsonObject.getString("subTypeName").equals("Foil") && foil) {
                        try {
                            double foilPrice = jsonObject.getDouble("marketPrice");
                            Log.d(Constants.TAG, "onPostExecute: " + foilPrice);
                            price = (float) foilPrice;
                        } catch (Exception e) {
                            e.printStackTrace();
                            price = 0;
                        } finally {
                            break;
                        }
                    } else if (jsonObject.getString("subTypeName").equals("Normal") && !foil) {
                        try {
                            double regularPrice = jsonObject.getDouble("marketPrice");
                            Log.d(Constants.TAG, "onPostExecute: " + regularPrice);
                            price = (float) regularPrice;
                        } catch (Exception e) {
                            e.printStackTrace();
                            price = 0;
                        } finally {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                price = 0;
            }

            mPriceCallback.onCardFound(null);
        }
    }
}
