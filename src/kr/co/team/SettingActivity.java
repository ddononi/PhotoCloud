package kr.co.team;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
/**
 *	환경 설정 엑티비티
 */
public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 *	알람발생 변경 처리
	 */

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		// 알람발생 키가 변경여부에 따라 서비스 중지 혹은 서비스를 구동시킨다.
        if (key.equals("alarm")) {
        	boolean isSetAlarm = sharedPreferences.getBoolean("alarm", true);

			Intent serviceIntent = new Intent(this, PhotoCheckService.class);
    		if (isSetAlarm) { 				// 알람체크가 되어 있으면 서비스시작
    			stopService(serviceIntent); //  서비스중지

    			startService(serviceIntent);
    			Log.i("photo", "start service");
    		}else{
    			stopService(serviceIntent); // 먼저 서비스를 중지한후 다시 시작
    		}
        }

	}
}