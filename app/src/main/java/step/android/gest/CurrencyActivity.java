package step.android.gest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.text.ParseException;
import java.util.ArrayList;

public class CurrencyActivity extends AppCompatActivity {
    private String bank_url ;

    private TextView tvContent ;
    private String contentBuffer ;
    private ArrayList<Rate> rates ;
    private String[] ccs;
    private DatePicker datePicker;

    private final Runnable displayContent = () -> {
        //tvContent.setText( "Count Rate : " + currencyArray.length() + "\n"  + contentBuffer ) ;
        tvContent.setText( contentBuffer ) ;
    } ;

    private final Runnable selectCurrencies = () -> {
        StringBuilder sb = new StringBuilder();
        for (Rate rate : rates){
            for (String cc : ccs){
                if (rate.getCc().equals( cc )){
                    sb.append( rate ) ;
                    sb.append('\n') ;
                }
            }
        }
        contentBuffer = sb.toString() ;
        runOnUiThread( displayContent ) ;
    } ;


    private final Runnable parseContent = () -> {
        try{

            JSONArray currencyArray = new JSONArray(contentBuffer);
            rates = new ArrayList<>();
            for (int i = 0; i < currencyArray.length(); ++i ){
                JSONObject rate = currencyArray.getJSONObject( i );
                rates.add(new Rate(rate));
            }
            ccs = new String[] {"USD", "EUR"} ;
           // contentBuffer = getString(R.string.count_message, rates.size());
            new Thread( selectCurrencies ).start();
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
        try (InputStream stream = new URL(bank_url).openStream()) {
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
           // runOnUiThread( displayContent ) ;

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

        datePicker = findViewById(R.id.datePicker) ;

        bank_url = getString(R.string.bank_url, GetCurrentDateString()) ;
        new Thread( openUrl ).start() ;

        findViewById(R.id.btnDate).setOnClickListener(v -> {

            int m = datePicker.getMonth() + 1 ;
            int d = datePicker.getDayOfMonth() ;
            int y = datePicker.getYear();
            String comboDate = ""
                    + y
                    + (m < 10 ? "0" : "") + m
                    + (d < 10 ? "0" : "") + d ;

            Date pickerDate = new Date(y,m,d);
            Date currentDate = GetCurrentDate();

            if (currentDate.compareTo(pickerDate) < 0){
                //Log.e("There are no courses for this date", pickerDate.toString());
                contentBuffer = "There are no courses for this date" ;
                runOnUiThread( displayContent ) ;
            }
            else{
                bank_url = getString(R.string.bank_url, comboDate );
                new Thread( openUrl ).start() ;
            }

        } ) ;

    }

    @NonNull
    private String GetCurrentDateString() {

       Date date = new Date( );
        int m = date.getMonth() +1;
        int d = date.getDate();
        int y = date.getYear() + 1900;
        return  ""
                + y
                + (m < 10 ? "0" : "") + m
                + (d < 10 ? "0" : "") + d ;
    }

    private Date GetCurrentDate() {

        Date date = new Date( );
        int m = date.getMonth() +1;
        int d = date.getDate();
        int y = date.getYear() + 1900;

        return  new Date(y,m,d);
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

/*
Задание: отобразить курсы наиболее популярных валют: Евро и Доллар
- в начале работы приложения определить сегодняшнюю дату, подставить в bankUrl
- при отображении курса указать дату:
   16.02.2022
   Доллар - ...
   Евро - ....
- реализовать конструктор Rate, принимающий JSONObject; перенести разбор полей в него
- если по запросу возвращается пустой ответ (дата будущая), выводить соотв. сообщение:
   16.02.3022
   На эту дату курсов нет
 */