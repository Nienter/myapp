package com.xbcx.view;


import com.xbcx.library.R;

import android.content.Context; 
import android.content.res.TypedArray; 
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas; 
import android.graphics.Color; 
import android.graphics.Paint; 
import android.graphics.Path; 
import android.graphics.PorterDuff; 
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode; 
import android.graphics.RectF; 
import android.util.AttributeSet; 
import android.widget.ImageView; 
 
public class RoundAngleImageView extends ImageView { 
 
    private Paint paint; 
    private int roundWidth = 2; 
    private int roundHeight = 2; 
    private Paint paint2; 
    
    private Canvas	mCanvas;
    private Bitmap	mBmp;
 
    public RoundAngleImageView(Context context, AttributeSet attrs, int defStyle) { 
        super(context, attrs, defStyle); 
        init(context, attrs); 
    } 
 
    public RoundAngleImageView(Context context, AttributeSet attrs) { 
        super(context, attrs); 
        init(context, attrs); 
    } 
 
    public RoundAngleImageView(Context context) { 
        super(context); 
        init(context, null); 
    } 
     
    private void init(Context context, AttributeSet attrs) { 
         
        if(attrs != null) {    
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundAngleImageView);  
            roundWidth= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_roundWidth, roundWidth); 
            roundHeight= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_roundHeight, roundHeight);
            a.recycle();
        }else { 
            float density = context.getResources().getDisplayMetrics().density; 
            roundWidth = (int) (roundWidth*density); 
            roundHeight = (int) (roundHeight*density); 
        }  
        
        mCanvas = new Canvas();
        
        paint = new Paint(); 
        paint.setColor(Color.WHITE); 
        paint.setAntiAlias(true); 
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT)); 
         
        paint2 = new Paint(); 
        paint2.setXfermode(null); 
    } 
     
    @Override 
    public void draw(Canvas canvas) { 
        //Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888); 
    	if(mBmp == null){
    		super.draw(canvas);
    	}else{
    		mCanvas.setBitmap(mBmp);
            mCanvas.drawColor(0x00000000,Mode.CLEAR);
            super.draw(mCanvas); 
            drawLiftUp(mCanvas); 
            drawRightUp(mCanvas); 
            drawLiftDown(mCanvas); 
            drawRightDown(mCanvas); 
            canvas.drawBitmap(mBmp, 0, 0, paint2);
    	} 
    }
     
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(w > 0 && h > 0){
			try{
				mBmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			}catch(OutOfMemoryError e){
				e.printStackTrace();
			}
		}
	}

	private void drawLiftUp(Canvas canvas) { 
        Path path = new Path(); 
        path.moveTo(getPaddingLeft(), roundHeight + getPaddingTop()); 
        path.lineTo(getPaddingLeft(), getPaddingTop()); 
        path.lineTo(roundWidth + getPaddingLeft(), getPaddingTop()); 
        path.arcTo(new RectF( 
                getPaddingLeft(),  
                getPaddingTop(),  
                roundWidth*2 + getPaddingLeft(),  
                roundHeight*2 + getPaddingTop()),  
                -90,  
                -90); 
        path.close(); 
        canvas.drawPath(path, paint); 
    } 
     
    private void drawLiftDown(Canvas canvas) { 
        Path path = new Path(); 
        path.moveTo(getPaddingLeft(), getHeight()-roundHeight - getPaddingBottom()); 
        path.lineTo(getPaddingLeft(), getHeight() - getPaddingBottom()); 
        path.lineTo(roundWidth + getPaddingLeft(), getHeight() - getPaddingBottom()); 
        path.arcTo(new RectF( 
                getPaddingLeft(),  
                getHeight()-roundHeight*2 - getPaddingBottom(),  
                0+roundWidth*2 + getPaddingLeft(),  
                getHeight() - getPaddingBottom()),  
                90,  
                90); 
        path.close(); 
        canvas.drawPath(path, paint); 
    } 
     
    private void drawRightDown(Canvas canvas) { 
        Path path = new Path(); 
        path.moveTo(getWidth() - roundWidth - getPaddingRight(), getHeight() - getPaddingBottom()); 
        path.lineTo(getWidth() - getPaddingRight(), getHeight() - getPaddingBottom()); 
        path.lineTo(getWidth() - getPaddingRight(), getHeight() - roundHeight - getPaddingBottom()); 
        path.arcTo(new RectF( 
                getWidth()-roundWidth*2 - getPaddingRight(),  
                getHeight()-roundHeight*2 - getPaddingBottom(),  
                getWidth() - getPaddingRight(),  
                getHeight() - getPaddingBottom()), 0, 90); 
        path.close(); 
        canvas.drawPath(path, paint); 
    } 
     
    private void drawRightUp(Canvas canvas) { 
        Path path = new Path(); 
        path.moveTo(getWidth() - getPaddingRight(), roundHeight + getPaddingTop()); 
        path.lineTo(getWidth() - getPaddingRight(), getPaddingTop()); 
        path.lineTo(getWidth()-roundWidth - getPaddingRight(), getPaddingTop()); 
        path.arcTo(new RectF( 
                getWidth()-roundWidth*2 - getPaddingRight(),  
                getPaddingTop(),  
                getWidth() - getPaddingRight(),  
                roundHeight*2 + getPaddingTop()),  
                -90,  
                90); 
        path.close(); 
        canvas.drawPath(path, paint); 
    } 
 
} 
