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
	private int NOTIFICATION_ID = 1;	// 앱 아이디값
	private SharedPreferences sp;				// 소리 및 진동 설정
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("photo", "broadcast catch!!");
		sp = PreferenceManager.getDefaultSharedPreferences(context);	// 환경설정값 가져오기
		showNotification(context, R.drawable.icon);	// 통지하기
	}

	/**
	 *	상태바에 알람을 알리고 확인시 MyScheduleActivity로 이동시킨다.
	 * @param context
	 * @param statusBarIconID
	 * 		상태바에 나타낼 아이콘
	 */
	private void showNotification(Context context, int statusBarIconID) {
		// MyScheduleActivity 로 엑티비티 설정

		Intent contentIntent = new Intent(context, GalleryActivity.class);
		// 알림클릭시 이동할 엑티비티 설정
		PendingIntent theappIntent = PendingIntent.getActivity(context, 0,contentIntent, 0);
		CharSequence title = "포토업로드알림"; 				// 알림 타이틀
		CharSequence message = "새로운 사진이 등록되었습니다."; 	// 알림 내용

		Notification notif = new Notification(statusBarIconID, null,
				System.currentTimeMillis());

		notif.flags |= Notification.FLAG_AUTO_CANCEL;			// 클릭시 사라지게
		notif.defaults |= Notification.DEFAULT_LIGHTS;			// led도 키자
		notif.defaults |= Notification.FLAG_ONLY_ALERT_ONCE; 	//알림 소리를 한번만 내도록
		//	진동알람을 설정했으면 진동을 울린다.
		if( sp.getBoolean("vibration", true) ){
			   //진동 객체 생성
			Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(2000); //1초 동안 진동

		}

		//	소리알람을 설정했으면 기본 알람 벨소리를 울린다.
		if( sp.getBoolean("sound", true) ){
			notif.defaults |= Notification.DEFAULT_SOUND;
		}

		notif.setLatestEventInfo(context, title, message, theappIntent);	// 통지바 설정
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(this.NOTIFICATION_ID, notif);	// 통지하기
	}
}
