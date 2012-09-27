package kr.co.team;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import kr.co.myutils.MyUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends BaseActivity {
	String mSdcardPath;
	TextView tv = null;
	MyFTPClient mFtp = null;
	AsyncTaskFileUpload fileupload = null;
	DeviceInfo mDeviceInfo = null;
	ImageButton mImgBtn = null;
	EditText mMessage = null;
	LocationManager mLm = null;
	Geocoder geoCoder = null;
	String selectedFile = null;
	ListView mListView = null;
	MyListAdapter adapter = null;
	ArrayList<String> fileList;
	Boolean result = false;
	String message;
	String[] storeFiles = new String[MAX_ATTACH];
	String[] receiveFiles = new String[MAX_ATTACH];
	String latitude = "";		// 위도
	String longitude = "";		// 경도
	String privacyAllow;		// 좌표 수락 여부
	Location location;			
	SharedPreferences settings = null;	// 공유환경

	int TAKE_PICTURE = 2068003;	// 사진 촬영 결과 코드
	
	// 다이얼로그 번호
	final int FILE_CHOICE_DIALOG = 2;
	final int NO_NETWORK_DIALOG = 3;
	final int CHECK_GPS_DIALOG = 5;
	final int FAIL_CONNECT_DIALOG = 8;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// 타이틀바를 없앤다.
		setContentView(R.layout.main);
		this.init(); // 초기화

		if (getLastNonConfigurationInstance() == null) {
			if (!checkNetWork(false)) {
				finish(); // 네트워크 연결이 안될때는 프로그램 종료
			}
		}

		gotoLocationSetting();
		getLocation(true); // 현재 위치를 한번 받아온다.

	}
	

	/*
	 * 초기 설정
	 */
	private void init() {
		// sdcard path 가져오기
		String extState = Environment.getExternalStorageState();
		if (extState.equals(Environment.MEDIA_MOUNTED)) {	
			mSdcardPath = Environment.getExternalStorageDirectory().getPath();
		} else {
			mSdcardPath = Environment.MEDIA_UNMOUNTED;
		}

		// mDeviceInfo = new DeviceInfo();
		// mDeviceInfo.setDeviceInfo((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE));

		findViewById(R.id.send).setOnClickListener(mClickListener);
		findViewById(R.id.attach_btn).setOnClickListener(mClickListener);
		mMessage = (EditText)findViewById(R.id.message);
		mListView = (ListView) findViewById(R.id.scannedList);
		fileList = new ArrayList<String>();
		restore(); // 화면이 바꼈을때 리스트의 파일명들을 가져옴
		adapter = new MyListAdapter(PictureActivity.this,
				R.layout.picture_list, fileList);
		mListView.setAdapter(adapter);
		mListView.setStackFromBottom(true);

		for (int i = 0; i < MAX_ATTACH; i++) {
			storeFiles[i] = ""; // 보관 파일명
			receiveFiles[i] = ""; // 수신 파일명;
		}

		gotoLocationSetting(); // 위치 설정이 비활성화시 설정 화면으로 이동

	}	

	/*
	 * private boolean checkSettingLocation(){ LocationManager locationManager =
	 * (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	 * if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
	 * locationManager.isProviderEnabled (LocationManager.NETWORK_PROVIDER)) {
	 * return true; } return false; }
	 */

	/**
	 * 네트워크망을 사용가능한지 혹은 연결되어있는지 확인한다.
	 * msgFlag가 false이면 현재 연결되어 있는 네트워크를 알려준다.
	 * 네트워크망 연결 불가시 사용자 에게 다이얼로그창을 띄어 알린다.
	 * @param msgFlag
	 * 		Toast 메세지  사용여부
	 * @return
	 *		네트워크 사용가능 여부
	 */
	private boolean checkNetWork(boolean msgFlag) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// boolean isWifiAvail = ni.isAvailable();
		boolean isWifiConn = ni.isConnectedOrConnecting();
		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		// boolean isMobileAvail = ni.isAvailable();
		boolean isMobileConn = ni.isConnectedOrConnecting();
		if (isWifiConn) {
			if (msgFlag == false) {
				Toast.makeText(PictureActivity.this, "Wi-Fi망에 접속중입니다.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (msgFlag == false) {
				Toast.makeText(PictureActivity.this, "3G망에 접속중입니다.",
						Toast.LENGTH_SHORT).show();
			}
		}

		if (!isMobileConn && !isWifiConn) {
			/*
			 * 네트워크 연결이 되지 않을경우 이전 화면으로 돌아간다.
			 */
			showDialog(NO_NETWORK_DIALOG);
			return false;
		}
		return true;

	}

	// 전송 및 사진 버튼 처리
	Button.OnClickListener mClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.attach_btn:
				// 최대 3개의 파일만 전송이 가능하게 함
				if (fileList.size() < MAX_ATTACH) {
					showDialog(FILE_CHOICE_DIALOG);
				} else { // 이미 3개이면
					new AlertDialog.Builder(PictureActivity.this)
							.setMessage("최대 " + MAX_ATTACH + "개의 이미지만 포함 가능합니다.")
							.setTitle("알림").setCancelable(false)
							.setPositiveButton("확인", null).show();
				}
				break;
			// 신고 전송 부분
			case R.id.send:
				/*
				 * 다이얼로그를 열어 사용자 위치 정보 승인여부 확인
				 */
				
				if( fileList.size() <= 0){	// 파일이 없으면..
					Toast.makeText(PictureActivity.this, "선택된 사진이 없습니다.", Toast.LENGTH_SHORT).show();
					break;
				}

				new AlertDialog.Builder(PictureActivity.this)
						.setTitle("위치정보 승인여부")
						.setSingleChoiceItems(R.array.location_allow, -1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										allowLocation(which);
										dialog.dismiss();
									}
								}).setNegativeButton("취소", null).show();
				break;

			}
		}

	};

	/**
	 * URI로 부터 실제 파일 경로를 가져온다.
	 * 
	 * @param uriPath
	 *            URI : URI 경로
	 * @return String : 실제 파일 경로
	 */
	public String getRealImagePath(Uri uriPath) {
		String[] proj = { MediaStore.Images.Media.DATA };

		Cursor cursor = managedQuery(uriPath, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String path = cursor.getString(index);
		// path = path.substring(5);
		// 일부 motorola등등에서 시작 path에 '/mnt/' 가 없을수 있음..
		return path.replace("/mnt/", "");
	}

	
	/**
	 * 사진촬영 혹은 사진선택번호를 받아 해당 intent로 넘긴다.
	 * @param which
	 * 		
	 */
	public void attachFileFilter(int which) {
		Intent intent = null;
		intent = new Intent();
		switch (which) {
		case 0: // 사진 촬영이면

			// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(intent, TAKE_PICTURE);
			break;
		case 1: // 사진 선택이면
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(Uri.parse("*.jpg"), "image/*");
			startActivityForResult(intent, ACTION_RESULT);
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent) 파일 선택 액티비티 결과 전송
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		// 사진 촬영선택에 uri가 없을 경우 bitmap을 이용해 파일을 만들어 준다.
		if (requestCode == TAKE_PICTURE && data.getData() == null) {
			String tmpFile = getDateTime() + ".jpg";
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			try {
				// bitmap을 jpg로 압축해주자
				bm.compress(CompressFormat.JPEG, 100,
						openFileOutput(tmpFile, MODE_PRIVATE));
			} catch (FileNotFoundException e) {
				new AlertDialog.Builder(PictureActivity.this)
						.setMessage("선택한 파일을 가져올수가 없습니다.").setTitle("알림")
						.setPositiveButton("확인", null).show();
			}

			selectedFile = PictureActivity.this.getFilesDir() /* 앱 어플리케이션 저장 위치 */
					+ "/" + tmpFile;
		} else {
			selectedFile = getRealImagePath(data.getData());
		}
		// 중복 파일인지 체크
		for (String matchFile : fileList) {
			if (matchFile.equals(selectedFile)) {
				new AlertDialog.Builder(PictureActivity.this)
						.setMessage("이미 포함된 파일입니다.").setTitle("알림")
						.setPositiveButton("확인", null).show();
				return;
			}
		}
		// 파일 길이 체크
		String file = selectedFile.substring(selectedFile.lastIndexOf("/") + 1);
		if (file.length() >= MAX_FILE_NAME_LENGTH) {
			new AlertDialog.Builder(PictureActivity.this)
					.setMessage("파일명이 너무 깁니다.\n파일이름은 100자 이내로 해주세요.")
					.setTitle("알림").setPositiveButton("확인", null).show();
			return;
		}

		File tmpfile = null;
		tmpfile = new File(selectedFile); // File 객체 생성
		int fileSize = (int) tmpfile.length(); // File 객체의 length() 메서드로 파일 길이
												// 구하기
		if (fileSize > MAX_FILE_SIZE) {
			int size = MAX_FILE_SIZE / 1024 / 1024;
			new AlertDialog.Builder(PictureActivity.this)
					.setMessage(
							"파일 용량은 " + String.valueOf(size) + "MB 이내로 해주세요")
					.setTitle("알림").setPositiveButton("확인", null).show();
			return;
		}

		fileList.add(selectedFile);
		// 어댑터에게 데이터 변경을 알림
		adapter.notifyDataSetChanged();
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		super.onCreateDialog(id);
		switch (id) {
		case FILE_CHOICE_DIALOG:	// 파일 선택 다이얼로그
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(PictureActivity.this)
					.setTitle("첨부파일 선택")
					.setItems(R.array.attach,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// 첨부파일 선택 처리 메소드
									attachFileFilter(which);
								}
							}).setNegativeButton("취소", null).show();

		case NO_NETWORK_DIALOG:	// 네트워크 연결 없음 알림
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(this)
					.setTitle("알림")
					.setMessage(
							"Wifi 혹은 3G망이 연결되지 않았거나 "
									+ "원활하지 않습니다.네트워크 확인후 다시 접속해 주세요!")
					.setPositiveButton("닫기",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss(); // 닫기
									Intent intent = new Intent(
											PictureActivity.this,
											StartActivity.class);
									startActivity(intent);
									finish();
								}
							}).show();

		case CHECK_GPS_DIALOG:	// GPS 활성 여부 다이얼로그
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(this)
					.setMessage(
							" 위치 무선 네트워크 사용  혹은 GPS가 비활성화 되어있습니다. 설정화면으로 이동 하시겠습니까?")
					.setCancelable(false)
					.setTitle("알림")
					.setPositiveButton("이동",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									moveConfigGPS();
								}
							})
					.setNegativeButton("취소",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).show();

		case FAIL_CONNECT_DIALOG:	// 서버 연결 실패 다이얼로그
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(PictureActivity.this)
					.setMessage("서버와의 연결을 실패 했습니다.\n네트워크 상태 체크후 다시 전송해 주세요")
					.setTitle("알림")
					.setCancelable(false)
					.setPositiveButton("확인",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									moveTaskToBack(true);
									finish();
								}
							}).show();
		}
		return null;
	}

	/**
	 * gps혹은 network 위치수신이 되어있으면 다이얼로그 뛰음
	 */
	private void gotoLocationSetting() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| !locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			//alertCheckGPS();
			showDialog(CHECK_GPS_DIALOG);
		}
	}

	// 위치정보 승인여부 다이얼로그 의 선택후 처리 함수
	private void allowLocation(int which) {

		double[] array = null;
		switch (which) {
		case 0:
			// gotoLocationSetting();
			array = getLocation(false); // 위치 정보 가져오기
			if (array == null) { // 위치 정보를 못받았을 경우
				privacyAllow = "N";
				Toast.makeText(PictureActivity.this,
						"현재 위치를 찾을수 없어 위치전송 미승인으로 발송 됩니다.", Toast.LENGTH_LONG)
						.show();
			} else {
				latitude = String.valueOf(array[0]);
				longitude = String.valueOf(array[1]);
			}
			privacyAllow = "Y";
			fileUpload();
			break;
		case 1:
			privacyAllow = "N";
			fileUpload();
			break;
		case 2:
			// gotoLocationSetting();
			array = getLocation(false); // 위치 정보 가져오기
			if (array == null) { // 위치 정보를 못받았을 경우
				Toast.makeText(PictureActivity.this, "현재 위치를 찾을수 없습니다.",
						Toast.LENGTH_LONG).show();
				break;
			} else {
				// 지도 uri 생성 및 zoom 설정
				String geoURI = String.format("geo:%f,%f?z=%d", array[0],
						array[1], ZOOM_DEEP);
				Uri geo = Uri.parse(geoURI);
				Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
				startActivity(geoMap);
			}

		}
	}

	/**
	 * 파일 업로드 실행 처리
	 * AsynTask를 이용하여 thread로 처리한다.
	 */
	private void fileUpload() {
		fileupload = new AsyncTaskFileUpload();
		mLockScreenRotation(); // 업로드중 화면 고정 처리
		fileupload.execute();
	}

	/**
	 * 공유설정환경 지움
	 */
	private void clearPrefer() {
		// 설정 지움
		SharedPreferences.Editor prefEditor = settings.edit();
		// 전화번호는 그대로 저장하자
		String cellNum = settings.getString("cellNum", "");
		prefEditor.clear();
		prefEditor.putString("cellNum", cellNum);
		prefEditor.commit();

	}

	private void setPreferences() { // 공유 설정 가져오기
		/*
		 * String savedMessage = settings.getString("message", "");
		 * ((TextView)findViewById(R.id.reportMessage)).setText(savedMessage);
		 * //Toast.makeText(PictureActivity.this, savedMessage,
		 * Toast.LENGTH_LONG).show(); String filename; File f = null;
		 * if(settings.contains("file0") == true){ filename =
		 * settings.getString("file0", ""); f = new File(filename); if(
		 * f.isFile() ){ // 파일이 존재하면 리스트에 넣어줌 fileList.add( filename ); }
		 * 
		 * } if(settings.contains("file1") == true){ filename =
		 * settings.getString("file1", ""); f = new File(filename); if(
		 * f.isFile() ){ // 파일이 존재하면 리스트에 넣어줌 fileList.add( filename ); } }
		 * if(settings.contains("file2") == true){ filename =
		 * settings.getString("file2", ""); f = new File(filename); if(
		 * f.isFile() ){ // 파일이 존재하면 리스트에 넣어줌 fileList.add( filename ); } }
		 * 
		 * adapter.notifyDataSetChanged(); // 어댑터에게 알려줌
		 */
	}


	// GPS 설정화면으로 이동
	private void moveConfigGPS() {
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptionsIntent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * ) 화면 회전시 텍스트 입력 내용을 사라지지 않게
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("files", fileList); // 선택된 파일 리스트 저장
		return map;
	}

	@SuppressWarnings("unchecked")
	private void restore() {
		Object obj = getLastNonConfigurationInstance();
		if (obj != null) {
			// Map형태로 리턴했기때문에 casting 해서 사용한다.
			HashMap<String, Object> map = (HashMap<String, Object>) obj;
			this.fileList = (ArrayList<String>) map.get("files");
		}
	}

	/*
	 * @Override protected void onDestroy() { Log.d(DEBUG_TAG, "onDestroy" +
	 * " isFinishing : " +isFinishing()); if(isFinishing()){ worker.interrupt();
	 * worker=null; } super.onDestroy(); }
	 */

	/*
	 * private static boolean isAvailableGps(Context context) { LocationManager
	 * lm = (LocationManager)context.getSystemService(
	 * Context.LOCATION_SERVICE); return
	 * lm.isProviderEnabled(LocationManager.GPS_PROVIDER); }
	 */

	/*
	 * private static boolean isAvailableLocation(Context context) {
	 * LocationManager lm = (LocationManager)context.getSystemService(
	 * Context.LOCATION_SERVICE); return
	 * lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER); }
	 */

	/*
	 * 사용자 위치의 현재 좌표 얻어오기
	 */
	private double[] getLocation(boolean msgFlag) {

		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		/*
		 * Criteria criteria = new Criteria();
		 * criteria.setAccuracy(Criteria.ACCURACY_FINE);// 정확도
		 * criteria.setPowerRequirement(Criteria.POWER_HIGH); // 전원 소비량
		 * criteria.setAltitudeRequired(false); // 고도 사용여부
		 * criteria.setBearingRequired(false); //
		 * criteria.setSpeedRequired(false); // 속도
		 * criteria.setCostAllowed(true); // 금전적비용
		 */
		// String provider = locationManager.getBestProvider(criteria, true);
		String provider;
		// gps 가 켜져 있으면 gps로 먼저 수신
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
			locationManager.requestLocationUpdates(provider, 100, 0,
					loclistener);// 현재정보를 업데이트
			location = locationManager.getLastKnownLocation(provider);
		} else { // 없으면 null
			location = null;
		}

		if (location == null) {

			// 무선 네크워트를 통한 위치 설정이 안되어 있으면 그냥 null 처리
			if (!(locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
				return null;
			}

			// 네트워크로 위치를 가져옴
			provider = LocationManager.NETWORK_PROVIDER;
			// criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			// provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			locationManager.requestLocationUpdates(provider, 1000, 10,
					loclistener);
			if (msgFlag == false) {
				Toast.makeText(PictureActivity.this,
						"실내에 있거나 GPS를 이용할수 없어  네트워크를 통해 현재위치를 찾습니다.",
						Toast.LENGTH_LONG).show();
			}
			if (location == null) {
				return null;
			}
		}
		// 위도, 경도 가져오기
		double[] array = { location.getLatitude(), location.getLongitude() };
		return array;
	}

	// 메세지 저장
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (isFinishing() == true) { // 프로그램이 끝날경우... 화면 회전시 저장 방지
			SharedPreferences setting = getSharedPreferences(PREFERENCE,
					MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = setting.edit();
			for (int i = 0; i < fileList.size(); i++) {
				prefEditor.putString("file" + i, fileList.get(i));
			}
			prefEditor.commit();
		}
	}

	// 위치 리스너 처리 
	private final LocationListener loclistener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.w(DEBUG_TAG, "onLocationChanged");
			// getLocation();
			PictureActivity.this.location = location;
		}

		public void onProviderDisabled(String provider) {
			Log.w(DEBUG_TAG, "onProviderDisabled");
		}

		public void onProviderEnabled(String provider) {
			Log.w(DEBUG_TAG, "onProviderEnabled");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.w(DEBUG_TAG, "onStatusChanged");
		}
	};

	/*
	 * ftp 연결 설정
	 */
	private boolean connectFTP() {
		mFtp = new MyFTPClient(SERVER_IP, SERVER_FTP_PORT, FTP_NAME,
				FTP_PASSWORD);
		if (!mFtp.connect()) {
		//	Toast.makeText(PictureActivity.this,
		//			"서버연결 실패!\n네트워크 상태 체크 후  다시 시도해 주세요", Toast.LENGTH_SHORT)
		//			.show();
			return false;
		}

		if (!mFtp.login()) {
		//	Toast.makeText(PictureActivity.this, "로그인 실패!", Toast.LENGTH_SHORT)
		//			.show();
			mFtp.logout();
			return false;
		}
		
		mFtp.cd(FTP_PATH);
		return true;
	}

	/*
	 * 파일 이름에 붙여줄 날자
	 */
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/*
	 * 기존 파일의 확장자를 새로운 파일에 붙여줌
	 */
	private String getExtension(String oldFile, String sep) {
		// 확장자 가져오기
		int index = oldFile.lastIndexOf(sep);
		String ext = oldFile.substring(index).toLowerCase();
		return ext;
	}
	
	/**
	 * 회전이 다시 화면을 불러오는걸 방지하기위해 스크린을 잠가준다. 
	 */
	private void mLockScreenRotation() {
		// Stop the screen orientation changing during an event
		switch (this.getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}
	
		


	/**
	 *	파일 전송 및 위치 전송 처리 클래스
	 *
	 */
	private class AsyncTaskFileUpload extends
			AsyncTask<Object, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(Boolean result) {	// 전송 완료후
			// 모든 파일이 전송이 완료되면 다이얼로그를 닫는다.
			dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			if( PictureActivity.this.mFtp.isConnected()){	// 연결이 되어 있으면 
				PictureActivity.this.mFtp.logout(); // 로그 아웃
			}
			// 파일 전송 결과를 출력
			if (result) { // 파일 전송이 정상이면
				Intent intent = new Intent(PictureActivity.this,
						ResultActivity.class);
				intent.putExtra("message", PictureActivity.this.message);
				intent.putExtra("date", getDateTime());
				intent.putExtra("allow", PictureActivity.this.privacyAllow);
				startActivity(intent);
				PictureActivity.this.finish();

			} else {
				Toast.makeText(PictureActivity.this, "파일 전송 실패!\n 네트워크 상태 및 서버상태를 체크하세요",
						Toast.LENGTH_LONG).show();
				// 전송 실패 처리 해야됨
			}

			PictureActivity.this
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // 화면
																							// 고정
																							// 해제
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute() 파일 전송중 로딩바 나타내기
		 */
		@Override
		protected void onPreExecute() {	// 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.
			dialog = ProgressDialog.show(PictureActivity.this, "전송중",
					"사용자 환경에 따라 전송 속도가 다를수 있습니다." + " 잠시 기다려주세요", true);
			 dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[]) 비동기 모드로 전송
		 */
		@Override
		protected Boolean doInBackground(Object... params) {	// 전송중

			// TODO Auto-generated method stub
			boolean result = true;

			if (!PictureActivity.this.checkNetWork(true)) { // 네트워크 상태 체크
				return false;
			}

			if (!PictureActivity.this.connectFTP()) { // ftp 연결이 안되면
				return false;
			}
			
			if (!MyUtils.checkVersion("photoclude", "1.3")) {
				finish();
			}			
			
			// http 로 보낼 이름 값 쌍 컬랙션
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			DeviceInfo di = DeviceInfo
					.setDeviceInfo((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));	// 디바이스 정보 얻어괴
			try {
				/* 파일 업로드 */
				for (int i = 0; i < PictureActivity.this.fileList.size(); i++) {
					String tmpFile = PictureActivity.this.fileList.get(i);
					storeFiles[i] = tmpFile
							.substring(tmpFile.lastIndexOf("/") + 1); // 실제 파일명만	 가졍옴
					// 이름 바꾸기 yyyymmdd_hhmmss_Cellnum_01.xxx
					receiveFiles[i] = getDateTime() + "_" + di.getDeviceNumber() + i + getExtension(tmpFile, ".");
					Log.d(DEBUG_TAG, storeFiles[i]);
					Log.d(DEBUG_TAG, "size" + i);
					// 파일 업로드
					if (!PictureActivity.this.mFtp.upload(tmpFile,
							receiveFiles[i])) {
						// Toast.LENGTH_SHORT).show();
						result = false;
					} else {
						 int j = i +1;
						 vars.add(new BasicNameValuePair("filename" + j, receiveFiles[i]));	// 파일이름
					}
				}
				
				// 메세지 체크
				String message ="";
				if(!TextUtils.isEmpty(mMessage.getText().toString())){	// 메세지 내용이 있으면 메시지를 넣어준다.
					message = new String(mMessage.getText().toString().getBytes("UTF-8"));
				}
				
				SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(PictureActivity.this);
				String tag = defaultSharedPref.getString("tag_filter", "전체");	
				
				// HTTP GET 메서드를 이용하여 데이터 업로드 처리
				vars.add(new BasicNameValuePair("lat", latitude));	// 위도
	            vars.add(new BasicNameValuePair("lon", longitude));	// 경도
	            vars.add(new BasicNameValuePair("phone", di.getDeviceNumber()));	
	            vars.add(new BasicNameValuePair("device_id", di.getMyDeviceID()));	
	            vars.add(new BasicNameValuePair("tag", tag));	// 태그            
	            vars.add(new BasicNameValuePair("message", message));	// 메세지
	            
	            String url = "http://" + SERVER_IP + "/pic_project/insert.php"; 
	           //+ URLEncodedUtils.format(vars, null);
	            
	            HttpPost request = new HttpPost(url);	
	            request.setEntity(new UrlEncodedFormEntity(vars, "UTF-8"));
	            try {

	                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                HttpClient client = new DefaultHttpClient();
	                String responseBody = client.execute(request, responseHandler);	// 전송

	                if (responseBody.equals("ok")) {
	                	//	Toast.makeText(getBaseContext(), responseBody,
	    				//		Toast.LENGTH_LONG).show();	  
	    				  Log.i(DEBUG_TAG, responseBody);
	    				  result = true;
	                }
	            } catch (ClientProtocolException e) {
	                Log.e(DEBUG_TAG, "Failed to get playerId (protocol): ", e);
	                result = false;
	            } catch (IOException e) {
	                Log.e(DEBUG_TAG, "Failed to get playerId (io): ", e);
	                result = false;
	            }
				
				
			} catch (Exception e) {
				result = false;
				dialog.dismiss(); // 프로그레스 다이얼로그 닫기
				Log.e(DEBUG_TAG, "파일 업로드 에러", e);
			}

			return result;
		}

	}
	

	@Override
	public void onBackPressed() {	//  뒤로 가기버튼 클릭시 종료 여부
		// TODO Auto-generated method stub
		//super.onBackPressed();
		finishDialog(this);
		
	}	

}
