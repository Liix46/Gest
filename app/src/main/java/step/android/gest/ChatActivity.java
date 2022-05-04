package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    private TextView tvChat ;
    private EditText etAuthor ;
    private EditText etMessage ;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_chat) ;

        tvChat = findViewById(R.id.tvChat) ;
        etAuthor = findViewById(R.id.etAuthor) ;
        etMessage = findViewById(R.id.etMessage) ;

        findViewById(R.id.chatLayout).setOnTouchListener((v,event) -> {
            hideSoftKeyboard() ;
            return true ;
        } ) ;

        findViewById(R.id.buttonSend).setOnClickListener(v -> {

            String author = String.valueOf(etAuthor.getText());
            String msg = String.valueOf(etMessage.getText());
            String request = String.format("http://chat.momentfor.fun/?author=%s&msg=%s",author, msg);

            Toast.makeText(this, request, Toast.LENGTH_SHORT).show();

            StringBuilder sb = new StringBuilder();
            sb.append(tvChat.getText());
            sb.append('\n');
            sb.append(etMessage.getText());
            tvChat.setText(sb);
            return;
        } );

    }

    private void hideSoftKeyboard(){
        ( (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE) )
                .hideSoftInputFromWindow(
                        getCurrentFocus()
                                .getWindowToken(),0) ;
    }
}