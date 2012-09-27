package kr.co.team;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

public class PhotoCheckService extends Service implements Runnable{
	// 내 담벼락 게시물 정보가 들어있는 uri
	private String requestUrl = "http://"+ BaseActivity.SERVER_IP + "/pic_project/check_upload.php";
	private Handler handler = new Handler();	// 업데이트 핸들러	
	private int repeatTime;						// 업데이트 주기
	private SharedPreferences mPrefs; 
	private String oldIdx;	// 이전 아이디값
	
	SharedPreferences defaultSharedPref;
	/** 서비스가 실행될때  */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    //StrictMode.setThreadPolicy(policy);
		
		// setting 에서 업데이트 주기를 가져온다.
		defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		//	알람이 설정되어 있는지 확인

		if(defaultSharedPref.getBoolean("alarm", false) == false){
			stopSelf();
			return 0;
		}

		mPrefs = getSharedPreferences("photo", MODE_PRIVATE);
		oldIdx = mPrefs.getString("idx", null);
		
		repeatTime = Integer.valueOf(defaultSharedPref.getString("repeat", "10"));	

		// 일정간격으로 아이디값 추출
		handler.postDelayed(this, 5000 );
		Log.i("photo", "starting service!!");
		return Service.START_STICKY;
	}

	private String getIndexFromUrl(){
		String tag = defaultSharedPref.getString("tag_filter", "전체");		
		try {
            HttpPost request = new HttpPost(requestUrl);	
			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair("tag", tag));
            request.setEntity(new UrlEncodedFormEntity(vars, "UTF-8"));			
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpClient client = new DefaultHttpClient();
            String responseBody = client.execute(request, responseHandler);	// 전송
			Log.i("photo", responseBody.replaceAll("\\D", ""));              
            return responseBody.replaceAll("\\D", "");
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
			Log.d("photo", "error service!!");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.d("photo", "error service!!");
		} finally {

		}
		return null;
	}

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("dservice", "stop!");
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if(oldIdx == null){	
				oldIdx = getIndexFromUrl();
				return;
			}
			

			if( oldIdx.equals(getIndexFromUrl()) == false){
				oldIdx = getIndexFromUrl();
				Log.d("photo","do receiver");
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("idx", oldIdx);
				editor.commit();
				Log.i("photo", "catch~");
				// 브로드케스트 리시버에 보낼 팬딩인텐트, 이전 팬딩인텐트가 있으면 취소하고 새로 실행
				Intent i = new Intent(getBaseContext(), PhotoReceiver.class);
				//i.putExtra("id",checkUpdateWall());	// 아이디값도 같이 보낸다.
				PendingIntent sender = PendingIntent.getBroadcast(getBaseContext(),
						0,  i, PendingIntent.FLAG_CANCEL_CURRENT);				
				sender.send();		// 브로드케스팅

			}
		}catch (Exception e) {
			// TODO: handle exception
		}finally{
			handler.postDelayed(this, 1000 * 60 * repeatTime );
		}
		
	}


}
