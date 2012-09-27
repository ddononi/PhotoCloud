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
	// �� �㺭�� �Խù� ������ ����ִ� uri
	private String requestUrl = "http://"+ BaseActivity.SERVER_IP + "/pic_project/check_upload.php";
	private Handler handler = new Handler();	// ������Ʈ �ڵ鷯	
	private int repeatTime;						// ������Ʈ �ֱ�
	private SharedPreferences mPrefs; 
	private String oldIdx;	// ���� ���̵�
	
	SharedPreferences defaultSharedPref;
	/** ���񽺰� ����ɶ�  */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    //StrictMode.setThreadPolicy(policy);
		
		// setting ���� ������Ʈ �ֱ⸦ �����´�.
		defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		//	�˶��� �����Ǿ� �ִ��� Ȯ��

		if(defaultSharedPref.getBoolean("alarm", false) == false){
			stopSelf();
			return 0;
		}

		mPrefs = getSharedPreferences("photo", MODE_PRIVATE);
		oldIdx = mPrefs.getString("idx", null);
		
		repeatTime = Integer.valueOf(defaultSharedPref.getString("repeat", "10"));	

		// ������������ ���̵� ����
		handler.postDelayed(this, 5000 );
		Log.i("photo", "starting service!!");
		return Service.START_STICKY;
	}

	private String getIndexFromUrl(){
		String tag = defaultSharedPref.getString("tag_filter", "��ü");		
		try {
            HttpPost request = new HttpPost(requestUrl);	
			// http �� ���� �̸� �� �� �÷���
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			vars.add(new BasicNameValuePair("tag", tag));
            request.setEntity(new UrlEncodedFormEntity(vars, "UTF-8"));			
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpClient client = new DefaultHttpClient();
            String responseBody = client.execute(request, responseHandler);	// ����
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
				// ��ε��ɽ�Ʈ ���ù��� ���� �ҵ�����Ʈ, ���� �ҵ�����Ʈ�� ������ ����ϰ� ���� ����
				Intent i = new Intent(getBaseContext(), PhotoReceiver.class);
				//i.putExtra("id",checkUpdateWall());	// ���̵𰪵� ���� ������.
				PendingIntent sender = PendingIntent.getBroadcast(getBaseContext(),
						0,  i, PendingIntent.FLAG_CANCEL_CURRENT);				
				sender.send();		// ��ε��ɽ���

			}
		}catch (Exception e) {
			// TODO: handle exception
		}finally{
			handler.postDelayed(this, 1000 * 60 * repeatTime );
		}
		
	}


}
