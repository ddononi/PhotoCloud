package kr.co.team;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *	디바이스 부팅 캐치후 알람 서비스 구동
 */
public class BootBroadCastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Log.i("photoclude", "Intent received");
			context.startService(new Intent(context, PhotoCheckService.class));
		}
	}
}
