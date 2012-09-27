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
 * �⺻ ���� ���̽� ��Ƽ��Ƽ
 *
 */
public class BaseActivity extends Activity {
    /* Debug setting */
    public static final String DEBUG_TAG = "pic";
    
    /* Server setting */
    public static final String SERVER_IP = "jng0419.cafe24.com";	// ************�ش� ftp ip �� ����***********************// 
    public static final int SERVER_FTP_PORT = 21;
   // public static final int SERVER_TCP_PORT = 5379;
    public static final String FTP_NAME = "jng0419";			// ************�ش� ftp id �� ����***********************// 
    public static final String FTP_PASSWORD = "qwerty01";		// ************�ش� ftp pass �� ����***********************// 
    public static final int MAX_SERVER_CONNECT_COUNT = 5;	//	�ִ� ���� ���� ȸ��
    public static final String FTP_PATH = "/www/pic_project/image_storage/";	//	ftp path
    
    /* Preferences setting */
    public static final String PREFERENCE = "Prefs";
    public static final String APP_VERSION = "1.0";
    public static final String PUBLISH_VERSION = "1.00d";
    public static final String APP_NAME = "��������";
    public static final String PGNAME = "PICTURE";
    public static final String VERION_ID = "PICTURE.1.0";
    public static final int MAX_ATTACH = 3;			//  �ִ� ���� ����
    public static final int ZOOM_DEEP = 17;			//  ���� �� ����
    public static final int ACTION_RESULT = 0;
    public static final String RECEIVE_OK = "ok";	// 
    public static final int MAX_TIME_LIMIT = 2000;	//	�ִ� ���� Ÿ��
    public static final int MAX_FILE_NAME_LENGTH = 100;	// �ִ� ���� �̸�
    public static final int MAX_FILE_SIZE = 52428800;	// �ִ� ���� ���� ���� ������ 50 Mb

    /*
     * ����
     */
    public void finishDialog(Context context){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage("���� �Ͻðڽ��ϱ�?")
		.setPositiveButton("����", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				moveTaskToBack(true);
				finish();
				android.os.Process.killProcess(android.os.Process.myPid() ); 
			}
		}).setNegativeButton("���",null).show();
    }
    
    /*
     *	�� ����
     */
    public void appInfoDialog(Context context){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("").setMessage( APP_NAME + " ver." + PUBLISH_VERSION )
		.setPositiveButton("Ȯ��",null).show();
    } 
    
    /*
     * 	����
     */
    public void appHelpDialog(Context context){
    	String str = getResources().getString( R.string.app_help );
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle("����").setMessage(str)
		.setPositiveButton("Ȯ��",null).show();
    }     
    
	/**
	 * �޴�Ű�� ������ �߻��Ǵ� �ɼǸ޴� ó��
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

		case R.id.setting:		// ����
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

