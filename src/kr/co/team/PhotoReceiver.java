package kr.co.team;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

public class PhotoReceiver extends BroadcastReceiver {
	private int NOTIFICATION_ID = 1;	// �� ���̵�
	private SharedPreferences sp;				// �Ҹ� �� ���� ����
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("photo", "broadcast catch!!");
		sp = PreferenceManager.getDefaultSharedPreferences(context);	// ȯ�漳���� ��������
		showNotification(context, R.drawable.icon);	// �����ϱ�
	}

	/**
	 *	���¹ٿ� �˶��� �˸��� Ȯ�ν� MyScheduleActivity�� �̵���Ų��.
	 * @param context
	 * @param statusBarIconID
	 * 		���¹ٿ� ��Ÿ�� ������
	 */
	private void showNotification(Context context, int statusBarIconID) {
		// MyScheduleActivity �� ��Ƽ��Ƽ ����

		Intent contentIntent = new Intent(context, GalleryActivity.class);
		// �˸�Ŭ���� �̵��� ��Ƽ��Ƽ ����
		PendingIntent theappIntent = PendingIntent.getActivity(context, 0,contentIntent, 0);
		CharSequence title = "������ε�˸�"; 				// �˸� Ÿ��Ʋ
		CharSequence message = "���ο� ������ ��ϵǾ����ϴ�."; 	// �˸� ����

		Notification notif = new Notification(statusBarIconID, null,
				System.currentTimeMillis());

		notif.flags |= Notification.FLAG_AUTO_CANCEL;			// Ŭ���� �������
		notif.defaults |= Notification.DEFAULT_LIGHTS;			// led�� Ű��
		notif.defaults |= Notification.FLAG_ONLY_ALERT_ONCE; 	//�˸� �Ҹ��� �ѹ��� ������
		//	�����˶��� ���������� ������ �︰��.
		if( sp.getBoolean("vibration", true) ){
			   //���� ��ü ����
			Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(2000); //1�� ���� ����

		}

		//	�Ҹ��˶��� ���������� �⺻ �˶� ���Ҹ��� �︰��.
		if( sp.getBoolean("sound", true) ){
			notif.defaults |= Notification.DEFAULT_SOUND;
		}

		notif.setLatestEventInfo(context, title, message, theappIntent);	// ������ ����
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(this.NOTIFICATION_ID, notif);	// �����ϱ�
	}
}
