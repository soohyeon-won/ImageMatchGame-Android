package com.example.samsung.memory;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MemoryGame extends AppCompatActivity {

    Context mContext = this;

    // 타이머 : 120초동안 1초 간격으로 시간이 줄어듬
    private static final int MILLISINFUTURE = 5*1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    static final int ROWS = 4;
    static final int COLUMNS = 4;
    static final int IMAGES = ROWS * COLUMNS;

    Drawable[] icon;
    Drawable back;
    int[][] grid = new int[ROWS][COLUMNS];

    List<Integer> iconNumbers = new ArrayList<Integer>();

    private Button[][] buttons = new Button[ROWS][COLUMNS];

    Button firstOpenButton = null;
    Button secondOpenButton = null;

    int firstOpenNumber = 0;

    int tries = 0;
    int pairs = ROWS * COLUMNS / 2;

    int pairsToFind = pairs;

    boolean secondOpen = false;
    boolean previousCorrect = true;

    int[] temp;

    GridLayout gridLayout;
    LinearLayout pointLayout;
    /* 시간 설정 */
    Button startBtn;
    TextView timerText;
    ProgressBar progressBar;
    Handler progressBarHandler = new Handler();
    static int timeCount;
    CountDownTimer countDownTimer;

    /* 점수 설정 */
    TextView pointTextView;
    double point;

    /* 팝업 화면 */
    private Dialog mDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        pointTextView = (TextView)findViewById(R.id.pointText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        gridLayout = (GridLayout)findViewById(R.id.grid);
        pointLayout = (LinearLayout)findViewById(R.id.pointLayout);
        timerText = findViewById(R.id.timerText);

        getImages();
        for(int i = 0; i < pairs; i++) {
            iconNumbers.add(i);
            iconNumbers.add(i);
        }

        shuffle();
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLUMNS; j++) {
                int tvId = mContext.getResources().getIdentifier("button"+(i)+""+(j),"id",mContext.getPackageName());
                String getId = "button"+i+""+j;
                buttons[i][j] = (Button)findViewById(tvId);
            }
        }
        timeCount = 4;

        point = 6;
        pointTextView.setText(point+"");

        // 시작버튼
        startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarHandler.post(new Runnable() {
                    public void run() {
                        startBtn.setVisibility(View.GONE);
                        gridLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        pointLayout.setVisibility(View.VISIBLE);

                        countDownTimer();
                        timerReset();
                    }
                });
            }
        });
    }

    int pointDownCount = 0;
    public void countDownTimer(){
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                timeCount = timeCount-1;
                    timerText.setText("제한 시간 : " + String.valueOf(timeCount / 60) + "분 " + String.valueOf(timeCount % 60) + "초");
                    progressBar.setProgress(timeCount);
                    pointDownCount++;

                    if (pointDownCount == 12) {
                        point = point - 0.5;
                        pointTextView.setText(String.valueOf(point));
                        Toast.makeText(mContext, "12초가 지나면 -0.5점!", Toast.LENGTH_LONG).show();
                        pointDownCount = 0;
                    }
                    Log.d("출력", timeCount+"");

                    if(timeCount==0){
                        //터치 막기.

                    }

            }
            public void onFinish() {
                Toast.makeText(mContext, "제한 시간 내에 못했음! -0.5점!", Toast.LENGTH_LONG).show();
                point = point - 0.5;
                popupGameDone();
            }
        };
    }

    public void timerReset() {
        countDownTimer.cancel();
        countDownTimer.start();
    }

    public void actionPerformed(View view) {

        //시도 횟수 카운트 증가
        tries++;
        int bi = 0, bj = 0;
        for ( int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLUMNS; j++) {
                if(view == buttons[i][j]) {
                    bi = i;
                    bj = j;
                }
            }
        }
        buttons[bi][bj].setBackground(icon[grid[bi][bj]]);

        if(secondOpen) {
            secondOpenButton = buttons[bi][bj];
            //맞았을 때.
            if(firstOpenNumber == grid[bi][bj]) {
                point += 0.5;
                    new Thread(new Runnable() {
                        public void run() {
                            pointTextView.post(new Runnable() {
                                public void run() {
                                    pointTextView.setText(String.valueOf(point));
                                }
                            });
                        }
                    }).start();

                //맞추면 클릭 못하게 함.
                buttons[temp[0]][temp[1]].setOnClickListener(null);
                buttons[bi][bj].setOnClickListener(null);
                pairsToFind--;

                // 더 이상 맞출 짝이 없을 때. (성공)
                if(pairsToFind == 0) {
                    for ( int i = 0; i < ROWS; i++) {
                        for(int j = 0; j < COLUMNS; j++) {
                            buttons[i][j].setClickable(false);
                        }
                    }

                    popupGameDone();
                }
                previousCorrect = true;
            }

            //틀렸을 때.
            else {
                for ( int i = 0; i < ROWS; i++) {
                    for(int j = 0; j < COLUMNS; j++) {
                        buttons[i][j].setClickable(false);
                    }
                }
                previousCorrect = false;
                //딜레이
                new Handler().postDelayed(new Runnable(){
                    public void run(){
                        if(!previousCorrect) {
                            firstOpenButton.setBackground(back);
                            secondOpenButton.setBackground(back);

                            for ( int i = 0; i < ROWS; i++) {
                                for(int j = 0; j < COLUMNS; j++) {
                                    buttons[i][j].setClickable(true);
                                }
                            }
                        }

                    }
                }, 650);

                //버튼 터치 활성화
                buttons[temp[0]][temp[1]].setClickable(true);
            }

            secondOpen = false;
        }
        //처음 클릭
        else {
            firstOpenButton = buttons[bi][bj];
            firstOpenNumber = grid[bi][bj];
            //버튼 터치 막기.
            buttons[bi][bj].setClickable(false);
            temp = new int[]{bi, bj};
            secondOpen = true;
        }
    }

    /**
     * 배열의 순서를 뒤섞는다.
     * 뒤섞은 다음에는 2차원 배열에 순서대로 저장하는 메소드.
     */
    private void shuffle() {
        java.util.Collections.shuffle(iconNumbers);

        int k = 0;
        for (int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLUMNS; j++) {
                grid[i][j] = iconNumbers.get(k++);
            }
        }
    }

    /* 게임 종료시 팝업. */
    private void popupGameDone() {
        final View innerView = getLayoutInflater().inflate(R.layout.popup_game_done, null);
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(innerView);
        setPopupSize(mDialog);

        // Back키 눌렀을 경우 Dialog Cancle 여부 설정
        mDialog.getWindow().setGravity(Gravity.BOTTOM);                                     //Dialog 위치 이동
        mDialog.setCanceledOnTouchOutside(true);                                            // Dialog 밖을 터치 했을 경우 Dialog 사라지지않게.
        mDialog.setCancelable(false);                                                       //화면 밖 터치 시 종료되지 않게.

        //최종 점수 표시.
        TextView totalPointText = (TextView)innerView.findViewById(R.id.totalPointText);
        int total = (int)point;
        totalPointText.setText(String.valueOf(total));

        //게임 종료시.
        Button doneButton = (Button)innerView.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                Intent intent = new Intent(mContext, demo.class);
                startActivity(intent);
                finish();
            }
        });
        mDialog.show();
    }

    /* 팝업 크기 설정 */
    private void setPopupSize(Dialog mDialog){
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    /* 다이얼로그 종료 */
    private void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * icon배열에 이미지 파일을 넣는 메소드.
     */
    private void getImages(){
        icon = new Drawable[IMAGES];
        icon[0] = getResources().getDrawable(R.drawable.angry_bird);
        icon[1] = getResources().getDrawable(R.drawable.bee);
        icon[2] = getResources().getDrawable(R.drawable.cat);
        icon[3] = getResources().getDrawable(R.drawable.dog);
        icon[4] = getResources().getDrawable(R.drawable.bird);
        icon[5] = getResources().getDrawable(R.drawable.clown_fish);
        icon[6] = getResources().getDrawable(R.drawable.cow);
        icon[7] = getResources().getDrawable(R.drawable.eagles);
        icon[8] = getResources().getDrawable(R.drawable.elephant);
        icon[9] = getResources().getDrawable(R.drawable.fish);
        icon[10] = getResources().getDrawable(R.drawable.jelly_fish);
        icon[11] = getResources().getDrawable(R.drawable.lion);
        icon[12] = getResources().getDrawable(R.drawable.owl);
        icon[13] = getResources().getDrawable(R.drawable.red_fish);
        icon[14] = getResources().getDrawable(R.drawable.snake);
        icon[15] = getResources().getDrawable(R.drawable.tiger);
        back = getResources().getDrawable(R.drawable.memory_game_back);
    }

}