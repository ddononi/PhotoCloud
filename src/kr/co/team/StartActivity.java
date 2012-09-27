package kr.co.team;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class StartActivity extends BaseActivity {
	String cellNum = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
       	this.init();	// 초기화 

		// setting 에서 업데이트 주기를 가져온다.
		SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		//	알람이 설정되어 있는지 확인
	//	if(defaultSharedPref.getBoolean("alarm", false)){
			Intent serviceIntent = new Intent(this, PhotoCheckService.class);
			stopService(serviceIntent); //  서비스중지
			startService(serviceIntent);
	//	}       	

    }
    
    /*
     *	초기설정
     *  전화번호 가져오기 및 전화번호 없을시 인증 하기 
     */
    private void init(){
        DeviceInfo di = DeviceInfo.setDeviceInfo((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)) ;
        this.cellNum = di.getDeviceNumber();
        Log.i(DEBUG_TAG, "tel :" + di.getDeviceNumber());
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 *  다음 화면으로 넘기기
	 * 
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		  if ( event.getAction() == MotionEvent.ACTION_DOWN ){
			  Log.i(DEBUG_TAG,event.toString());
			 Intent intent =  new Intent(StartActivity.this, PictureActivity.class);
			 startActivity(intent);
			 return true;
		  }
		  
		  return super.onTouchEvent(event);
		  
	}

        
}
