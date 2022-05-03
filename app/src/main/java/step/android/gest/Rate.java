package step.android.gest;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* ORM for currency rate :
"r030":36,
"txt":"Австралійський долар",
"rate":21.261,"cc":"AUD",
"exchangedate":"12.03.2022"

 */
public class Rate {
    private int r030 ;
    private String txt ;
    private double rate ;
    private String cc ;
    private Date exchangeDate ;

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat parser =
            new SimpleDateFormat("dd.MM.yyyy");


    public Rate(int r030, String txt, double rate, String cc, String exchangeDate)
            throws ParseException {
        this.setR030(r030);
        this.setTxt(txt);
        this.setRate(rate);
        this.setCc(cc);
        this.setExchangeDate(exchangeDate);
    }

    public Rate(JSONObject jsonObject) throws JSONException, ParseException {
        this.setR030(jsonObject.getInt( "r030"));
        this.setTxt(jsonObject.getString("txt" ));
        this.setRate(jsonObject.getDouble("rate" ));
        this.setCc(jsonObject.getString( "cc" ));
        this.setExchangeDate(jsonObject.getString( "exchangedate" ));
    }

    @NonNull
    @Override
    @SuppressLint("DefaultLocale")
    public String toString(){
        return String.format("%s %f", this.txt, this.rate) ;
    }

    public int getR030() {
        return r030;
    }

    public void setR030(int r030) {
        this.r030 = r030;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public Date getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(Date exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    public void setExchangeDate(String dateString) throws ParseException {
        this.exchangeDate = parser.parse( dateString ) ;
    }
}
