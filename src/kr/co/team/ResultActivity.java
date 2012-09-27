package kr.co.team;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

/**
 * ���� ��� ��Ƽ��Ƽ
 *
 */
public class ResultActivity extends BaseActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// Ÿ��Ʋ�ٸ� ���ش�.
		setContentView(R.layout.result);

	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		  if ( event.getAction() == MotionEvent.ACTION_DOWN ){
			  Log.i(DEBUG_TAG,event.toString());
			 Intent intent =  new Intent(ResultActivity.this, GalleryActivity.class);
			 startActivity(intent);
			 finish();
			 return true;
		  }
		  
		  return super.onTouchEvent(event);
		  
	}	
}
