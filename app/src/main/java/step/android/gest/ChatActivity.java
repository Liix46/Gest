package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class ChatActivity extends AppCompatActivity {

    private EditText etAuthor ;
    private EditText etMessage ;
    private LinearLayout chatContainer ;
    private Button buttonSend ;
    // Data Context
    private final ArrayList<ChatMessage> messages = new ArrayList<>() ;
    // URL:
    String chatUrl ;
    // URL response buffer
    private String urlResponse ;


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
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_chat) ;

        etAuthor = findViewById(R.id.etAuthor) ;
        etMessage = findViewById(R.id.etMessage) ;
        chatContainer = findViewById( R.id.chatContainer ) ;
        buttonSend = findViewById(R.id.buttonSend) ;
        buttonSend.setBackground(AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.button ));



        findViewById( R.id.chatContainer ).setOnTouchListener( (v, event) -> {
            if( event.getAction() == MotionEvent.ACTION_UP ) {
                v.performClick() ;
            }
            else {
                hideSoftKeyboard() ;
            }
            return true ;
        } ) ;

        findViewById( R.id.buttonSend ).setOnClickListener( this::sendButtonClick ) ;

        chatUrl = getString( R.string.chat_url_get ) ;
        new Thread( loadUrlResponse ).start() ;
    }

    private void sendButtonClick( View v ) {
        String author = etAuthor.getText().toString() ;
        if( author.length() == 0 ) {
            Toast.makeText(this, R.string.chat_author_empty, Toast.LENGTH_SHORT).show();
            return ;
        }
        String message = etMessage.getText().toString() ;
        if( message.length() == 0 ) {
            Toast.makeText(this, R.string.chat_message_empty, Toast.LENGTH_SHORT).show();
            return ;
        }
        chatUrl = getString(
                R.string.chat_url_send,
                author,
                message ) ;
        new Thread( loadUrlResponse ).start() ;
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
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        layoutParams.setMargins( 5,5,5,5 ) ;
        layoutParams.gravity = Gravity.START ;

        LinearLayout.LayoutParams myLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        myLayoutParams.setMargins( 5,5,5,5 ) ;
        myLayoutParams.gravity = Gravity.END ;

        chatContainer.removeAllViews() ;  // clear
        for( ChatMessage message : messages ) {
            TextView txt = new TextView(this );
            txt.setText( message.toChatString() ) ;

            txt.setPadding( 5, 5, 5, 5 ) ;
            String authorMessage = message.getAuthor();
            String currentAuthor = String.valueOf(etAuthor.getText());
            txt.setPadding( 30, 25, 30, 25 ) ;
            if (authorMessage.contentEquals( currentAuthor ) ){
                txt.setBackground( AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.my_message ) ) ;

                txt.setLayoutParams(myLayoutParams);
            }
            else {
                txt.setBackground( AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.message ) ) ;
                txt.setLayoutParams(layoutParams);
            }
            chatContainer.addView( txt ) ;
        }

        new Thread( () ->
                runOnUiThread( () ->
                        ((ScrollView)chatContainer.getParent()).fullScroll(
                                ScrollView.FOCUS_DOWN
                        ) ) ).start() ;
    }

    private boolean messagesContain( JSONObject obj ) throws JSONException {
        for (ChatMessage message : messages) {
            if (message.getId() == obj.getInt("id")) {
                return true;
            }
        }
        return false;
    }
}