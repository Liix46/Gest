package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;

public class CurrencyActivity extends AppCompatActivity {
    private final String CURRENCY_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    private TextView tvContent ;
    private String contentBuffer ;
    private JSONArray currencyArray ;
    private ArrayList<Rate> rates ;

    private Runnable displayContent = () -> {
        tvContent.setText( "Count Rate : " + currencyArray.length() + "\n"  + contentBuffer ) ;
       // tvContent.setText( contentBuffer ) ;
    } ;


    private final Runnable parseContent = () -> {
        try{
            currencyArray = new JSONArray( contentBuffer ) ;
            rates = new ArrayList<>();
            for ( int i = 0; i < currencyArray.length(); ++i ){
                JSONObject rate = currencyArray.getJSONObject( i );
                rates.add( new Rate(
                        rate.getInt( "r030"),
                        rate.getString("txt" ),
                        rate.getDouble("rate" ),
                        rate.getString( "cc" ),
                        rate.getString( "exchangedate" )

                ) ) ;
            }
            runOnUiThread( displayContent ) ;
        }
        catch (JSONException | ParseException exception){
            Log.e("Open URL: ", exception.getMessage() ) ;
            runOnUiThread(() ->
                    Toast.makeText(
                            CurrencyActivity.this,
                            exception.getMessage(),
                            Toast.LENGTH_SHORT).show()
            );
        }
    } ;

    private final Runnable openUrl = () -> {
        try (InputStream stream = new URL(CURRENCY_URL).openStream()) {
           // Read from stream
            StringBuilder sb = new StringBuilder() ;
            int sym ;
            while ( ( sym = stream.read() ) != -1){
                sb.append( ( char ) sym ) ;
            }
            //contentBuffer = sb.toString() ;
            contentBuffer = new String(
                    sb.toString().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8
            ) ;
            new Thread(parseContent).start();
            //runOnUiThread( displayContent ) ;

        }
        catch( Exception ex ){
            Log.e("Open URL: ", ex.getMessage() ) ;
            runOnUiThread(() ->
                    Toast.makeText(
                            CurrencyActivity.this,
                            ex.getMessage(),
                            Toast.LENGTH_SHORT).show()
            );
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        tvContent = findViewById(R.id.tvContent);
        tvContent.setMovementMethod( new ScrollingMovementMethod() ) ;

        new Thread( openUrl ).start();

    }


}

/*
Работа с Интернет
Приложение необходимо разрешение, заявленное в манифесте
<uses-permission android:name="android.permission.INTERNET"/>
если начальное приложение было установлено без разрешения, возможно,
его прийдется переустановить (удалить из устройства и поставить заново)

Для связи с интернет ресурсом используеься класс URL
Особенности:
    создание обьекта URL не открывает соединения (аналог - FILE)
    для открытия соединения испольщуется .openStream() и рекомендуется
    блок с автозакрытием try(){}
    открытие соединений не разрещено в UI потоке, неободимо создавать новый
    обращение к UI элементам разрешено только из UI потока, для делегирования
    к UI  потоку предусмотрен метод runOnUiThread( Runnable )
    по стандарту, Интернет данные передаються в кодировке ISO_8859_1
    однако, большинство JSON ресурсов использует UTF-8
    для перекодирования используется коструктор String( byte[], Charset )

----------
О скроллинге:
для реализации прокрутки необходимо
а) в разметке указать направления прокрутки
    android:scrollbars="vertical"
б) в коде установить действие при перетягивании в режим "прокрутка"
    tvContent.setMovementMethod( new ScrollingMovementMethod() ) ;
 */