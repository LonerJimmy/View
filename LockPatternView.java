package com.zjm.GestureLock.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;



import java.util.ArrayList;
import java.util.List;

import com.zjm.GestureLock.Utils.Point;

/**
 * Created by lenovo on 2015/9/8.
 */
public class LockPatternView extends View {

    private boolean codeWrong=true;
    private float padding;
    private float r;//鍦嗗湀鍗婂緞
    private boolean isFirstInit=true;
    private float w,h;//view鐨勫搴﹀拰楂樺害
    private Point[][] mPoint=new Point[3][3];
    private List<Point> sPoint=new ArrayList<Point>();//touch鍒扮殑鍦嗗湀鏁扮粍
    boolean movingNoPoint = false;//娌℃湁绉诲姩point
    private float moveingX,moveingY;//鎵嬫寚绉诲埌鐨勪綅缃�
    private boolean checking=false;//鏄惁缁х画璺熼殢鎵嬫寚婊戝姩杩涜缁樼敾
    private int passwordMinLength = 5;//瀵嗙爜浣嶆暟
    private boolean isTouch = true;
    private com.zjm.GestureLock.Utils.Paint mPaint=new com.zjm.GestureLock.Utils.Paint();

    public LockPatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockPatternView(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(isFirstInit){
            init();//绗竴娆″垵濮嬪寲
            drawBottomCircle(canvas);
        }
        drawBottomCircle(canvas);
        drawCanvas(canvas);
    }

    //灏嗗瘑鐮佷繚瀛樺埌sharedpreference涓�
    public void setPassWord(String passWord){

        SharedPreferences sp = this.getContext().getSharedPreferences("Gesture_Lock", this.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("password", passWord);//灏嗗瘑鐮佷繚瀛樺埌sharedpreference涓殑password涓�

        editor.commit();
    }


    private OnCompleteListener mCompleteListener;


    /**
     * @param mCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    public interface OnCompleteListener {

        public void onComplete(String password);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouch) {
            return false;
        }
        movingNoPoint = false;

        float mX=event.getX();
        float mY=event.getY();
        Point p=null;
        boolean isFinish=false;

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN://鎵嬫寚鎸変笅
                //reset();
                p = checkSelectPoint(mX, mY);
                if(p!=null){
                    checking=true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(checking){
                    p=checkSelectPoint(mX,mY);
                    if(p==null){//杩樻病婊戝埌涓嬩竴涓寜閽笂
                        movingNoPoint=true;
                        moveingX=mX;
                        moveingY=mY;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                p=checkSelectPoint(mX,mY);
                checking = false;
                isFinish=true;//鎵嬫寚缁撴潫绉诲姩
                break;
        }

        //鎵嬫寚杩樺湪绉诲姩
        if(!isFinish && checking && p!=null){
            int aa=crossPoint(p);
            if(aa==2){
                movingNoPoint = true;
                moveingX = mX;
                moveingY = mY;

            }else if(aa==0){
                p.state=Point.STATE_CHECK;
                sPoint.add(p);
            }
        }

        //鎵嬫寚绉诲姩缁撴潫
        if(isFinish){
            if (this.sPoint.size() == 1) {//瀵嗙爜鏄竴浣�
                this.reset();
            } else if (this.sPoint.size() < passwordMinLength
                    && this.sPoint.size() > 0) {//瀵嗙爜浣嶆暟涓嶆纭�
                // mCompleteListener.onPasswordTooMin(sPoints.size());
                if (mCompleteListener != null) {
                    //if (this.sPoint.size() >= passwordMinLength) {
                    mCompleteListener.onComplete(toPointString());
                    //}
                }
                error();
                //reset();
                postInvalidate();
                //clearPassword(5);
            } else {//瀵嗙爜璁剧疆鎴愬姛
                codeWrong=false;
                if (mCompleteListener != null) {
                    //if (this.sPoint.size() >= passwordMinLength) {
                    mCompleteListener.onComplete(toPointString());
                    //}
                }
                for(int x=0;x<sPoint.size();x++){
                    Log.e("jimmy_view", "code=" + sPoint.get(x).index);
                }
            }
        }

        this.postInvalidate();
        return true;
    }

    //灏嗛�涓殑鎸夐挳鐨刬ndex杞寲涓哄瓧绗︿覆锛屼腑闂寸敤,闅斿紑
    private String toPointString() {
        if (sPoint.size() >= passwordMinLength) {
            StringBuffer sf = new StringBuffer();
            for (Point p : sPoint) {
                sf.append(",");
                sf.append(p.index);
            }
            return sf.deleteCharAt(0).toString();
        } else {
            return "";
        }
    }

    public String getPassword() {
        SharedPreferences settings = this.getContext().getSharedPreferences(
                this.getClass().getName(), 0);
        return settings.getString("password", ""); // , "0,1,2,3,4,5,6,7,8"
    }

    public void clearPassword() {
            reset();
            postInvalidate();
    }

    //灏嗘墍鏈塩heck鐨刾oint鏀逛负error锛屽嵆灏嗙豢鑹茬殑鎸夐挳鍙樹负绾㈣壊error鎸夐挳
    public void error(){
        for (Point p : sPoint) {
            p.state = Point.STATE_CHECK_ERROR;
        }
    }

    //鍒ゆ柇绉诲姩鍒扮殑鐐逛笌鍓嶉潰涓�釜touch鐐规槸鍚﹂噸鍚�
    private int crossPoint(Point p) {

        if (sPoint.contains(p)) {
            if (sPoint.size() > 2) {
                if (sPoint.get(sPoint.size() - 1).index != p.index) {
                    return 2;
                }
            }
            return 1;
        } else {
            return 0;
        }
    }

    //閫夋嫨绂绘墜鎸噒ouch浣嶇疆鏈�繎鐨刾oint锛岃窛绂诲皬浜巖
    private Point checkSelectPoint(float x ,float y){
        for (int i = 0; i < mPoint.length; i++) {
            for (int j = 0; j < mPoint[i].length; j++) {
                Point p = mPoint[i][j];
                if (checkInRound(p.x, p.y, r, (int) x, (int) y)) {
                    return p;
                }
            }
        }
        return null;
    }

    private boolean checkInRound(float sx, float sy, float r, float x,
                                       float y) {
        return Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) < r;
    }

    private void reset() {
        for (Point p : sPoint) {
            p.state = Point.STATE_NORMAL;
        }
        sPoint.clear();
        this.enableTouch();
    }

    public void enableTouch() {
        isTouch = true;
    }

    public void disableTouch() {
        isTouch = false;
    }

    private void drawCanvas(Canvas canvas) {

        if (sPoint.size() > 0) {
            Point tp = sPoint.get(0);

            //灏唖Point涓瓨鍌ㄧ殑鎸夐挳杩炶捣鏉�
            for (int i = 0; i < sPoint.size(); i++) {
                Point p = sPoint.get(i);
                drawLine(canvas, tp, p);
                tp = p;
            }

            //濡備綍鎵嬫寚杩樺湪绉诲姩锛屽垝绾�
            if (this.movingNoPoint) {
                drawLine(canvas, tp, new Point((int) moveingX, (int) moveingY));
            }
        }
        drawTopCircle(canvas);
        //drawLine(canvas);

    }

    private void drawLine(Canvas canvas,Point a,Point b){

        if (a.state == Point.STATE_CHECK_ERROR) {
                Paint paint = new Paint();
                paint.setColor(mPaint.getErrorColor());
                paint.setStrokeWidth((float) 10.0);
                canvas.drawLine(a.x, a.y, b.x, b.y, paint);

        } else {
                Paint paint = new Paint();
                paint.setColor(mPaint.getTopColor());
                paint.setStrokeWidth((float) 10.0);
                canvas.drawLine(a.x, a.y, b.x, b.y, paint);
        }
    }

    private void drawTopCircle(Canvas canvas){
        for (int i = 0; i < mPoint.length; i++) {
            for (int j = 0; j < mPoint[i].length; j++) {
                Point p = mPoint[i][j];
                if (p.state == Point.STATE_CHECK) {
                    drawRightCircle(canvas, p);
                } else if (p.state == Point.STATE_CHECK_ERROR) {
                    drawErrorCircle(canvas,p);
                } else {

                }
            }
        }
    }

    private void drawErrorCircle(Canvas canvas,Point p){

        //澶栧湀绌哄績鍦�
        switch (mPaint.getStyle()){
            case 0://鏀粯瀹濇牱寮�
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);//绌哄績鍦�
                paint.setStrokeWidth(2);
                paint.setAntiAlias(true);
                paint.setColor(mPaint.getErrorColor());
                canvas.drawCircle(p.x, p.y, r, paint);

                Paint paint2 = new Paint();
                paint2.setStyle(Paint.Style.FILL);//瀹炲績鍦�
                paint2.setColor(mPaint.getErrorColor());

                canvas.drawCircle(p.x, p.y, r / 3, paint2);

                break;
            case 1:

                Paint paint4 = new Paint();
                paint4.setStyle(Paint.Style.FILL);//瀹炲績鍦�
                paint4.setColor(mPaint.getErrorColor());

                canvas.drawCircle(p.x, p.y, r / 3, paint4);

            case 2:

        }
    }

    //鐢荤豢鑹插渾鍦�
    private void drawRightCircle(Canvas canvas,Point p){

        switch (mPaint.getStyle()){
            case 0://鏀粯瀹濇牱寮�
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);//绌哄績鍦�
                paint.setStrokeWidth(2);
                paint.setAntiAlias(true);
                paint.setColor(mPaint.getTopColor());
                canvas.drawCircle(p.x, p.y, r, paint);

                Paint paint2 = new Paint();
                paint2.setStyle(Paint.Style.FILL);//瀹炲績鍦�
                paint2.setColor(mPaint.getTopColor());

                canvas.drawCircle(p.x, p.y, r / 3, paint2);

                break;
            case 1:

                Paint paint3 = new Paint();
                paint3.setStyle(Paint.Style.FILL);//瀹炲績鍦�
                paint3.setColor(mPaint.getTopColor());

                canvas.drawCircle(p.x, p.y, r / 3, paint3);

            case 2:

        }
        //paint.setStyle(Paint.Style.STROKE);//绌哄績鍦�

        //澶栧湀绌哄績鍦�
        /*
        paint.setStyle(Paint.Style.STROKE);//绌哄績鍦�
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);*/



        //涓棿瀹炲績鍦�

    }

    //鐢诲簳鑹插渾
    private void drawBottomCircle(Canvas canvas){
        Paint paint = new Paint();
        switch (mPaint.getStyle()){
            case 0://鏀粯瀹濇牱寮�
                paint.setStyle(Paint.Style.STROKE);//绌哄績鍦�
                paint.setStrokeWidth(2);
                paint.setAntiAlias(true);
                paint.setColor(mPaint.getBottomColor());
                //paint.setStrokeWidth(2);
                //paint.setAntiAlias(true);
                for (int i = 0; i < mPoint.length; i++) {
                    for (int j = 0; j < mPoint[i].length; j++) {
                        Point p = mPoint[i][j];
                        canvas.drawCircle(p.x,p.y,r,paint);
                    }
                }
                break;
            case 1:
                paint.setStyle(Paint.Style.FILL);//绌哄績鍦�
                paint.setStrokeWidth(2);
                paint.setAntiAlias(true);
                paint.setColor(mPaint.getBottomColor());
                //paint.setStrokeWidth(2);
                //paint.setAntiAlias(true);
                for (int i = 0; i < mPoint.length; i++) {
                    for (int j = 0; j < mPoint[i].length; j++) {
                        Point p = mPoint[i][j];
                        canvas.drawCircle(p.x,p.y,r/3,paint);
                    }
                }
            case 2:

        }
        //paint.setStyle(Paint.Style.STROKE);//绌哄績鍦�

    }

    public void setPaint(com.zjm.GestureLock.Utils.Paint paint){
        this.mPaint=paint;
    }

    private void init(){
        isFirstInit=false;

        w=this.getWidth();
        h=this.getHeight();

        float devide;
        if(h>w) {
            padding=w*5/23;
            devide = (w-padding) / 23;
        }else{
            padding=h*5/23;
            devide=(h-padding) /23;
        }
        r=(devide*5)/2;

        mPoint[0][0] = new Point(devide*2+padding/2,devide*2+padding/2);
        mPoint[0][1] = new Point(devide*(5+4+2)+padding/2, devide*2+padding/2);
        mPoint[0][2] = new Point(devide*(5+4+5+4+2)+padding/2, devide*2+padding/2);
        mPoint[1][0] = new Point(devide*2+padding/2, devide*(5+4+2)+padding/2);
        mPoint[1][1] = new Point(devide*(5+4+2)+padding/2, devide*(5+4+2)+padding/2);
        mPoint[1][2] = new Point(devide*(5+4+5+4+2)+padding/2, devide*(5+4+2)+padding/2);
        mPoint[2][0] = new Point(devide*2+padding/2, devide*(5+4+5+4+2)+padding/2);
        mPoint[2][1] = new Point(devide*(5+4+2)+padding/2, devide*(5+4+5+4+2)+padding/2);
        mPoint[2][2] = new Point(devide*(5+4+5+4+2)+padding/2, devide*(5+4+5+4+2)+padding/2);

        int k=1;
        for (Point[] ps : mPoint) {
            for (Point p : ps) {
                p.index = k;
                k++;
            }
        }

    }
}
