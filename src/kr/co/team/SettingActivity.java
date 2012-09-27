package kr.co.team;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
/**
 *	ȯ�� ���� ��Ƽ��Ƽ
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
	 *	�˶��߻� ���� ó��
	 */

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		// �˶��߻� Ű�� ���濩�ο� ���� ���� ���� Ȥ�� ���񽺸� ������Ų��.
        if (key.equals("alarm")) {
        	boolean isSetAlarm = sharedPreferences.getBoolean("alarm", true);

			Intent serviceIntent = new Intent(this, PhotoCheckService.class);
    		if (isSetAlarm) { 				// �˶�üũ�� �Ǿ� ������ ���񽺽���
    			stopService(serviceIntent); //  ��������

    			startService(serviceIntent);
    			Log.i("photo", "start service");
    		}else{
    			stopService(serviceIntent); // ���� ���񽺸� �������� �ٽ� ����
    		}
        }

	}
}