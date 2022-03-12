package step.android.gest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Random random;
    int _steps;
    int _level;
    TextView textView;
    SeekBar seekBar;
    final int complexity;
    TableLayout fieldLayout;
    Button buttonLevel;

    public MainActivity(){
        random = new Random();
        _steps = 0;
        complexity = 20;
       // complexity = 1;
        _level = 1;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // current field state:
        CharSequence[] cellText = new CharSequence[16] ;
        for (int i = 0; i < 16; ++i){
            cellText[i] = getCellByIndex(i).getText() ;
        }
        outState.putCharSequenceArray("fieldState", cellText) ;
        outState.putInt("stepsState", _steps);
        outState.putInt("complexityState", _level);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        _steps = savedInstanceState.getInt("stepsState");
        _level = savedInstanceState.getInt("complexityState");
        CharSequence[] cellText = savedInstanceState.getCharSequenceArray("fieldsState") ;
        for ( int i = 0; i < 16; ++i ){
            TextView cell = getCellByIndex(i);
            cell.setText(cellText[i]);
            if ( cellText[ i ].equals( "" ) ) {
                cell.setBackground(
                        AppCompatResources.getDrawable(
                                getApplicationContext(),
                                R.drawable.cell_shape
                ));
            } else {
                cell.setBackground(
                        AppCompatResources.getDrawable(
                                getApplicationContext(),
                                R.drawable.cell_0_shape
                ) );
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.fieldLayout).setOnTouchListener(
                new OnSwipeTouchListener(getApplicationContext()){
                    @Override
                    public void onSwipeRight(){
                        userMove(MoveDirection.RIGHT);
                    }
                    @Override
                    public void onSwipeLeft(){
                        userMove(MoveDirection.LEFT);
                    }
                    @Override
                    public void onSwipeTop(){
                        userMove(MoveDirection.TOP);
                    }
                    @Override
                    public void onSwipeBottom(){
                        userMove(MoveDirection.BOTTOM);
                    }
                }
        );
        buttonLevel = findViewById(R.id.buttonLevel);
        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                textView.setText(progress + " level");
                _level = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        buttonLevel.setOnClickListener(view -> {
           int level =  seekBar.getProgress();
           if (savedInstanceState == null){
               shuffle(level);
           }


           textView.setText("");
           buttonLevel.setVisibility(View.INVISIBLE);
           seekBar.setVisibility(View.INVISIBLE);
           fieldLayout.setVisibility(View.VISIBLE);
        });

        fieldLayout = findViewById(R.id.fieldLayout);
        fieldLayout.setVisibility(View.INVISIBLE);

    }

    /**
     * Move after and game checking
     * @param direction swipe direction
     */
    @SuppressLint("SetTextI18n")
    private void userMove(MoveDirection direction){
        if (moveCell( direction )){
            _steps++;
            textView.setText(_steps + " steps");
            if (isGameOver()){
                // Alert Dialog
                new AlertDialog.Builder(MainActivity.this )
                        .setTitle(R.string.game_over)
                        .setMessage("Count steps:\t "+ _steps + "\n\nPlay again?")
                        .setIcon( android.R.drawable.ic_dialog_dialer )
                        .setPositiveButton("Yes", (dialog, which) -> {
                            //shuffle( seekBar.getProgress());
                            textView.setText("1 level");
                            buttonLevel.setVisibility(View.VISIBLE);
                            seekBar.setVisibility(View.VISIBLE);
                            fieldLayout.setVisibility(View.INVISIBLE);
                            _steps = 0;
                        })
                        .setNegativeButton("No", (dialog, which) -> finish())
//                        .setNeutralButton("Rnd", (dialog, which) -> {
//                            if ( random.nextBoolean() ){
//                                shuffle(seekBar.getProgress());
//                            }
//                            else finish();
//                        })
                        .show();
            }
        }
        else {    // Если ход невозможен, то выводится Toast
            Toast.makeText(
                    MainActivity.this,
                    R.string.invalid_move,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Replaces cells in field
     * @param n random moves count
     */
    private void shuffle( int n ){

        int cnt = 0;
        MoveDirection[] moveDirectionValues = MoveDirection.values();
        while  (cnt < (n* complexity)){
            if (moveCell(
                    moveDirectionValues [
                            random.nextInt(moveDirectionValues.length)])){
                ++cnt ;
            }
        }
    }

    private boolean moveCell( MoveDirection direction){
        int emptyCellIndex = getEmptyCellIndex();
        //Log.d("onSwipeRight", emptyCellIndex + " | " + emptyCellIndex % 4 + "");

        int otherCellIndex = 0;
        switch (direction) {
            case BOTTOM:
                if (emptyCellIndex > 0 && emptyCellIndex < 5)
                    return false;
                otherCellIndex = emptyCellIndex == 0 ? 12 : emptyCellIndex - 4 ;
                break;
            case LEFT:
                if (emptyCellIndex % 4 == 0)
                    return false;
                otherCellIndex = emptyCellIndex == 15 ? 0 : emptyCellIndex + 1 ;
                break;
            case RIGHT:
                if (emptyCellIndex % 4 == 1){
                    return false;
                }
                otherCellIndex = emptyCellIndex == 0 ? 15 : emptyCellIndex - 1 ;
                break;
            case TOP:
                if (emptyCellIndex > 12 && emptyCellIndex < 16 || emptyCellIndex == 0){
                    return false;
                }
                otherCellIndex = emptyCellIndex == 12 ? 0 : emptyCellIndex + 4 ;
                break;
        }

        //isGameOver();
        SwapCells(otherCellIndex, emptyCellIndex);
        return true;
    }

    private boolean isGameOver(){

        for (int i = 1; i < 16; i++){
            if ( ! getCellByIndex(i).getText().equals("" + i ) ) {
                return false;
            }
        }
        return true;
    }

    private  void SwapCells(int cellIndex1, int cellIndex2){
        TextView cell  = getCellByIndex(cellIndex1);
        TextView cell0 = getCellByIndex(cellIndex2);

        Drawable bg = cell.getBackground();
        Drawable bg0 = cell0.getBackground();

        cell.setBackground(bg0);
        cell0.setBackground(bg);

        CharSequence txt = cell.getText();
        CharSequence txt0 = cell0.getText();
        cell.setText(txt0);
        cell0.setText(txt);
    }


    private TextView getCellByIndex (int index){
        return findViewById(
                getResources().getIdentifier(
                        "cell_" + index,
                        "id",
                        getPackageName()
                )
        );
    }


    private int getEmptyCellIndex(){
        for (int i = 0; i < 16; ++i){
            TextView cell = getCellByIndex(i);
            if (cell.getText().equals("")) return i ;
        }
        return  -1 ;
    }

    enum MoveDirection{
        BOTTOM,
        LEFT,
        RIGHT,
        TOP
    }
}

/*
Manifest
Файл манифеста соединяет в себе "декларации",
касающейся всего приложения:
- активности и их связи
- переключение / фиксация орентации экрана
- требуемые разрешения / доступ к модулям устройства
(Интернет, Контакты, Камера)

Жизненный цикл активности предусматривает пересоздания (вызов onCreate) при
    изменениях ориентациях ориентации устройства / размеров приложения
    определить первая ли это сборка или реакции на изменения можно при помощи
    параметра, передаваемого в onCreate - savedInstanceState
        null - первая сборка
        !null - непервая сборка
Средствами манифеста можно зафиксировать ориентацию активности, указав
android:screenOrientation="portrait"

-------
Активности
добавление через меню
в манифесте указываем атрибуты (опционально)
для перехода на новую активность
    а) если хотим сделать главной - меняем в монифесте <intent-filter>
    б) если просто запустить ( по нажатию кнопки) - создаем Intent, указываем
       класс новой активности и вызываем startActivity(intent)
Для указания отношений между активностями в манифесте записываем
android:parentActivityName="..." в атрибутах соответствуйщей активности
в таком случае на панели приложения будет автомотически формироваться "<-",
возвращающая на родительскую активность
-------
единицы размеров
dp - density pixel - приаязан к физическому размеру, разное кол-во
    для разной плотности экрана
sp - scalable pixel - увеличене с действием масштаба
( в основном для шрифтов)


 */