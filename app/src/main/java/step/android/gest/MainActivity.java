package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fieldLayout).setOnTouchListener(
                new OnSwipeTouchListener(getApplicationContext()){
                    @Override
                    public void onSwipeRight(){
                        int emptyCellIndex = getEmptyCellIndex();
                        Log.d("onSwipeRight", emptyCellIndex + " | " + emptyCellIndex % 4 + "");
                        if (emptyCellIndex % 4 == 1){
                            Toast.makeText(MainActivity.this,
                                    R.string.invalid_move,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int leftCellIndex = emptyCellIndex == 0
                                ? 15
                                : emptyCellIndex - 1 ;

                        SwapCells(leftCellIndex, emptyCellIndex);
                    }
                    @Override
                    public void onSwipeLeft(){
                        int emptyCellIndex = getEmptyCellIndex();
                        Log.d("Gest: onSwipeRight", emptyCellIndex + "");
                        if (emptyCellIndex % 4 == 0){
                            Toast.makeText(MainActivity.this,
                                    R.string.invalid_move,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int rightCellIndex =
                                emptyCellIndex == 15
                                ? 0
                                : emptyCellIndex + 1 ;

                        SwapCells(rightCellIndex,emptyCellIndex);
                    }
                    @Override
                    public void onSwipeTop(){
                        int emptyCellIndex = getEmptyCellIndex();
                        Log.d("Gest: onSwipeTop", emptyCellIndex + "");

                        if (emptyCellIndex > 12 && emptyCellIndex < 16 || emptyCellIndex == 0){
                            Toast.makeText(MainActivity.this,
                                    R.string.invalid_move,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int topCellIndex =
                                emptyCellIndex == 12
                                        ? 0
                                        : emptyCellIndex + 4 ;

                        SwapCells(topCellIndex,emptyCellIndex);
                    }
                    @Override
                    public void onSwipeBottom(){
                        int emptyCellIndex = getEmptyCellIndex();
                        Log.d("Gest: onSwipeBottom", emptyCellIndex + "");

                        if (emptyCellIndex > 0 && emptyCellIndex < 5){
                            Toast.makeText(MainActivity.this,
                                    R.string.invalid_move,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //int bottomCellIndex = emptyCellIndex += 4 ;

                        int bottomCellIndex =
                                emptyCellIndex == 0
                                    ? 12
                                    : emptyCellIndex - 4 ;

                        SwapCells(bottomCellIndex,emptyCellIndex);
                    }
                }
        );
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
}