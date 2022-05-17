package step.android.gest;

import androidx.annotation.NonNull;

import com.vdurmont.emoji.EmojiParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage
        implements Comparable<ChatMessage> {
    private int id;
    private String author;
    private String text;
    private Date moment;

    // Inner fields
    private boolean isDisplayed ;

    private final static SimpleDateFormat dtParser =
            new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm",
                    Locale.UK
            );

    private final static SimpleDateFormat dtParser2 =
            new SimpleDateFormat(
                    "hh:mm",
                    Locale.UK
            );

    // control + return

    public ChatMessage(@NonNull JSONObject object) throws JSONException, ParseException {
        setId(object.getInt("id"));
        setAuthor(object.getString("author"));
        setText(EmojiParser.parseToUnicode(object.getString("text")));
        setMoment(object.getString("moment"));
        setDisplayed( false ) ;
    }

    @Override
    public int compareTo( ChatMessage other ) {
        return this.moment.compareTo( other.getMoment() ) ;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getAuthor() {return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public Date getMoment() { return moment; }

    public void setMoment(Date moment) {
        this.moment = moment;
    }

    public void setMoment(String moment) throws ParseException {
        this.setMoment( dtParser.parse( moment ) ) ;
    }

    public boolean isDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(boolean displayed) {
        isDisplayed = displayed;
    }
    ///////////////


    @NonNull
    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", text='" + text + '\'' +
                ", moment=" + moment +
                '}';
    }
    public String toChatString() {
        StringBuilder stringBuilder = new StringBuilder(getAuthor());
        if (stringBuilder.length() + getText().length() < 25){
            stringBuilder.append(": ");
            stringBuilder.append(getText());
            stringBuilder.append(" ");
            stringBuilder.append(dtParser2.format( moment));
            return stringBuilder.toString();
        }
        return "" + getAuthor()
                + "\n" + getText()
                + "\n" + dtParser2.format( moment);
    }
}
