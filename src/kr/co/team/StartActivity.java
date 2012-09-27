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
       	this.init();	// �ʱ�ȭ 

		// setting ���� ������Ʈ �ֱ⸦ �����´�.
		SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		//	�˶��� �����Ǿ� �ִ��� Ȯ��
	//	if(defaultSharedPref.getBoolean("alarm", false)){
			Intent serviceIntent = new Intent(this, PhotoCheckService.class);
			stopService(serviceIntent); //  ��������
			startService(serviceIntent);
	//	}       	

    }
    
    /*
     *	�ʱ⼳��
     *  ��ȭ��ȣ �������� �� ��ȭ��ȣ ������ ���� �ϱ� 
     */
    private void init(){
        DeviceInfo di = DeviceInfo.setDeviceInfo((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)) ;
        this.cellNum = di.getDeviceNumber();
        Log.i(DEBUG_TAG, "tel :" + di.getDeviceNumber());
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 *  ���� ȭ������ �ѱ��
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
