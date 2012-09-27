package kr.co.team;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * 기본 설정 베이스 액티비티
 *
 */
public class BaseActivity extends Activity {
    /* Debug setting */
    public static final String DEBUG_TAG = "pic";
    
    /* Server setting */
    public static final String SERVER_IP = "jng0419.cafe24.com";	// ************해당 ftp ip 로 수정***********************// 
    public static final int SERVER_FTP_PORT = 21;
   // public static final int SERVER_TCP_PORT = 5379;
    public static final String FTP_NAME = "jng0419";			// ************해당 ftp id 로 수정***********************// 
    public static final String FTP_PASSWORD = "qwerty01";		// ************해당 ftp pass 로 수정***********************// 
    public static final int MAX_SERVER_CONNECT_COUNT = 5;	//	최대 서버 연결 회수
    public static final String FTP_PATH = "/www/pic_project/image_storage/";	//	ftp path
    
    /* Preferences setting */
    public static final String PREFERENCE = "Prefs";
    public static final String APP_VERSION = "1.0";
    public static final String PUBLISH_VERSION = "1.00d";
    public static final String APP_NAME = "사진전송";
    public static final String PGNAME = "PICTURE";
    public static final String VERION_ID = "PICTURE.1.0";
    public static final int MAX_ATTACH = 3;			//  최대 파일 갯수
    public static final int ZOOM_DEEP = 17;			//  지도 줌 깊이
    public static final int ACTION_RESULT = 0;
    public static final String RECEIVE_OK = "ok";	// 
    public static final int MAX_TIME_LIMIT = 2000;	//	최대 연결 타임
    public static final int MAX_FILE_NAME_LENGTH = 100;	// 최대 파일 이름
    public static final int MAX_FILE_SIZE = 52428800;	// 최대 사진 파일 전송 사이즈 50 Mb

    /*
     * 종료
     */
    public void finishDialog(Context context){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage("종료 하시겠습니까?")
		.setPositiveButton("종료", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				moveTaskToBack(true);
				finish();
				android.os.Process.killProcess(android.os.Process.myPid() ); 
			}
		}).setNegativeButton("취소",null).show();
    }
    
    /*
     *	앱 정보
     */
    public void appInfoDialog(Context context){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage( APP_NAME + " ver." + PUBLISH_VERSION )
		.setPositiveButton("확인",null).show();
    } 
    
    /*
     * 	도움말
     */
    public void appHelpDialog(Context context){
    	String str = getResources().getString( R.string.app_help );
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("도움말").setMessage(str)
		.setPositiveButton("확인",null).show();
    }     
    
	/**
	 * 메뉴키를 누를때 발생되는 옵션메뉴 처리
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {

		case R.id.setting:		// 설정
			intent = new Intent(getBaseContext(), SettingActivity.class);
			startActivity(intent);
			return true;
		case R.id.gallery:	
			intent = new Intent(this, GalleryActivity.class);
			startActivity(intent);	
			return true;
		}
		return false;
	}	
}

