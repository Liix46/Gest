package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.vdurmont.emoji.EmojiParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ChatActivity extends AppCompatActivity{
    private ImageView btEmoji, btSend;
    private EditText etMessage ;
    private  LinearLayout linear_layout;
    private LinearLayout chatContainer ;

    private String author;
    private Date dateMessage = null;

    // Data Context
    private final ArrayList<ChatMessage> messages = new ArrayList<>() ;
    // URL:
    String chatUrl ;
    // URL response buffer
    private String urlResponse ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_chat) ;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            author = extras.getString("login");
            //The key argument here must match that used in the other activity
        }

        btEmoji = findViewById(R.id.bt_emoji);
        btSend = findViewById(R.id.bt_send);
        etMessage = findViewById(R.id.etMessage) ;
        linear_layout = findViewById(R.id.linear_layout);

        chatContainer = findViewById( R.id.chatContainer ) ;

        EmojiManager.install(new GoogleEmojiProvider());

        EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(etMessage);

        btEmoji.setOnClickListener(view -> popup.toggle());

        findViewById( R.id.chatContainer ).setOnTouchListener( (v, event) -> {
            if( event.getAction() == MotionEvent.ACTION_UP ) {
                v.performClick() ;
            }
            else {
                hideSoftKeyboard() ;
            }
            return true ;
        } ) ;

        findViewById( R.id.bt_send).setOnClickListener( this::sendButtonClick ) ;

        chatUrl = getString( R.string.chat_url_get ) ;
        new Thread( loadUrlResponse ).start() ;
    }


    // URL response mapper:  String -> JSON -> messages
    private final Runnable mapUrlResponse = () -> {
        try {
            JSONObject response = new JSONObject( urlResponse ) ;
            int status = response.getInt( "status" ) ;
            if( status == 1 ) {
                JSONArray arr = response.getJSONArray( "data" ) ;
                boolean isUpdated = false ;
                for( int i = 0; i < arr.length(); ++i ) {
                    JSONObject obj = arr.getJSONObject( i ) ;
                    if( ! messagesContain( obj ) ) {
                        messages.add(
                                new ChatMessage( obj ) ) ;
                        isUpdated = true ;
                    }
                }
                if( isUpdated ) {
                    Collections.sort( messages ) ;
                    runOnUiThread( this::showMessagesInScroll ) ;
                }
            }
            else {
                Log.e( "mapUrlResponse: ", "Bad response status " + status ) ;
            }
        }
        catch( Exception ex ) {
            Log.e( "mapUrlResponse: ", ex.getMessage() ) ;
        }
    } ;

    // URL response loader
    private final Runnable loadUrlResponse = () -> {
        try( InputStream stream =
                     new URL( chatUrl )
                             .openStream()
        ) {
            StringBuilder sb = new StringBuilder() ;
            int sym ;
            while( ( sym = stream.read() ) != -1 ) {
                sb.append( (char) sym ) ;
            }
            urlResponse = new String(
                    sb.toString().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8
            ) ;
            new Thread( mapUrlResponse ).start() ;
        }
        catch( Exception ex ) {
            Log.e( "loadUrlResponse: ", ex.getMessage() ) ;
        }
    } ;

    private void sendButtonClick( View view ) {
        if( author.length() == 0 ) {
            Toast.makeText(this, R.string.chat_author_empty, Toast.LENGTH_SHORT).show();
            return ;
        }
        String message = etMessage.getText().toString();

        String result = EmojiParser.parseToAliases(message);

        if( message.length() == 0 ) {
            Toast.makeText(this, R.string.chat_message_empty, Toast.LENGTH_SHORT).show();
            return ;
        }
        chatUrl = getString(
                R.string.chat_url_send,
                author,
                result ) ;
        new Thread( loadUrlResponse ).start() ;

        EmojiTextView emojiTextView = (EmojiTextView) LayoutInflater
                .from(view.getContext())
                .inflate(
                        R.layout.emoji_text_view,
                        linear_layout,
                        false
                );

        emojiTextView.setText(etMessage.getText().toString());
        etMessage.getText().clear();
                chatContainer.addView( emojiTextView ) ;

    }

    private void hideSoftKeyboard() {
        View focusedView = getCurrentFocus() ;
        if( focusedView != null )
            ( (InputMethodManager)
                    getSystemService( INPUT_METHOD_SERVICE ) )
                    .hideSoftInputFromWindow(
                            focusedView.getWindowToken(), 0 ) ;
    }

    private void showMessagesInScroll() {

        LinearLayout.LayoutParams layoutDataParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        layoutDataParams.setMargins( 5,5,5,5 ) ;
        layoutDataParams.gravity = Gravity.CENTER_HORIZONTAL;

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        layoutParams.setMargins( 5,5,5,5 ) ;
        layoutParams.gravity = Gravity.START;


        LinearLayout.LayoutParams myLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        myLayoutParams.setMargins( 5,5,5,5 ) ;
        myLayoutParams.gravity = Gravity.END ;

        chatContainer.removeAllViews() ;  // clear
        for( ChatMessage message : messages ) {
            if (dateMessage == null){
                EmojiTextView txtTime = (EmojiTextView) LayoutInflater
                        .from(btSend.getContext())
                        .inflate(
                                R.layout.emoji_text_view,
                                linear_layout,
                                false
                        );
                txtTime.setPadding( 30, 25, 30, 25 ) ;
                SimpleDateFormat formatter= new SimpleDateFormat("dd-MM");
                dateMessage = message.getMoment();
                txtTime.setText(formatter.format(dateMessage));
                chatContainer.addView( txtTime ) ;
            }
            else{
                Date date1 = new Date(dateMessage.getYear(), dateMessage.getMonth(), dateMessage.getDate()); // Returns the current date and time
                Date date2 = new Date(message.getMoment().getYear(), message.getMoment().getMonth(), message.getMoment().getDate());
                if (date1.before(date2)){
                    EmojiTextView txtTime = (EmojiTextView) LayoutInflater
                            .from(btSend.getContext())
                            .inflate(
                                    R.layout.emoji_text_view,
                                    linear_layout,
                                    false
                            );
                    txtTime.setPadding( 30, 10, 30, 10 ) ;
                    SimpleDateFormat formatter= new SimpleDateFormat("dd-MM");
                    dateMessage = message.getMoment();
                    txtTime.setText(formatter.format(dateMessage));
                    txtTime.setLayoutParams(layoutDataParams);
                    txtTime.setBackground( AppCompatResources.getDrawable(
                            getApplicationContext(),
                            R.drawable.time_message ) ) ;

                    chatContainer.addView( txtTime ) ;

                }
            }
            if (! message.getText().equals("")){
                EmojiTextView txt = (EmojiTextView) LayoutInflater
                        .from(btSend.getContext())
                        .inflate(
                                R.layout.emoji_text_view,
                                linear_layout,
                                false
                        );

                txt.setText( message.toChatString() ) ;
                txt.setId(message.getId());
                String authorMessage = message.getAuthor();
                txt.setPadding( 30, 25, 30, 25 ) ; //outside
                if (authorMessage.contentEquals( author ) ){
                    txt.setBackground( AppCompatResources.getDrawable(
                            getApplicationContext(),
                            R.drawable.my_message ) ) ;

                    txt.setLayoutParams(myLayoutParams);
                    txt.setOnClickListener(this::messageClick);
                    txt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                }
                else {
                    txt.setBackground( AppCompatResources.getDrawable(
                            getApplicationContext(),
                            R.drawable.message ) ) ;
                    txt.setLayoutParams(layoutParams);
                    txt.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                }
                chatContainer.addView( txt ) ;
            }

        }
        new Thread( () ->
                runOnUiThread( () ->
                        ((ScrollView)chatContainer.getParent()).fullScroll(
                                ScrollView.FOCUS_DOWN
                        ) ) ).start() ;

    }

    private void messageClick( View v ) {

        TextView currentView = ( (TextView) v );
        //Log.println(Log.ASSERT, "\tNEW\t",currentView.getText().toString());
        Log.println(Log.ASSERT, "\tNEW\t", String.valueOf(currentView.getId()));

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder
                .setTitle("Do you want delete message?")
                .setIcon(android.R.drawable.ic_delete)
                .setView(R.layout.activity_dialog)
                .setMessage(currentView.getText().toString())
                .setPositiveButton("YES", (dialogInterface, i) -> deleteMessage(currentView))
                .setNegativeButton("NO", null)
                .create()
                .show();
    }

    private boolean messagesContain( JSONObject obj ) throws JSONException {
        for (ChatMessage message : messages) {
            if (message.getId() == obj.getInt("id")) {
                return true;
            }
        }
        return false;
    }

    public void deleteMessage(TextView view){
        messages.removeIf(x -> x.getId() == view.getId());
        runOnUiThread( this::showMessagesInScroll ) ;
    }
}

