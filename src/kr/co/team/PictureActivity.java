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
	String latitude = "";		// ����
	String longitude = "";		// �浵
	String privacyAllow;		// ��ǥ ���� ����
	Location location;			
	SharedPreferences settings = null;	// ����ȯ��

	int TAKE_PICTURE = 2068003;	// ���� �Կ� ��� �ڵ�
	
	// ���̾�α� ��ȣ
	final int FILE_CHOICE_DIALOG = 2;
	final int NO_NETWORK_DIALOG = 3;
	final int CHECK_GPS_DIALOG = 5;
	final int FAIL_CONNECT_DIALOG = 8;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// Ÿ��Ʋ�ٸ� ���ش�.
		setContentView(R.layout.main);
		this.init(); // �ʱ�ȭ

		if (getLastNonConfigurationInstance() == null) {
			if (!checkNetWork(false)) {
				finish(); // ��Ʈ��ũ ������ �ȵɶ��� ���α׷� ����
			}
		}

		gotoLocationSetting();
		getLocation(true); // ���� ��ġ�� �ѹ� �޾ƿ´�.

	}
	

	/*
	 * �ʱ� ����
	 */
	private void init() {
		// sdcard path ��������
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
		restore(); // ȭ���� �ٲ����� ����Ʈ�� ���ϸ���� ������
		adapter = new MyListAdapter(PictureActivity.this,
				R.layout.picture_list, fileList);
		mListView.setAdapter(adapter);
		mListView.setStackFromBottom(true);

		for (int i = 0; i < MAX_ATTACH; i++) {
			storeFiles[i] = ""; // ���� ���ϸ�
			receiveFiles[i] = ""; // ���� ���ϸ�;
		}

		gotoLocationSetting(); // ��ġ ������ ��Ȱ��ȭ�� ���� ȭ������ �̵�

	}	

	/*
	 * private boolean checkSettingLocation(){ LocationManager locationManager =
	 * (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	 * if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
	 * locationManager.isProviderEnabled (LocationManager.NETWORK_PROVIDER)) {
	 * return true; } return false; }
	 */

	/**
	 * ��Ʈ��ũ���� ��밡������ Ȥ�� ����Ǿ��ִ��� Ȯ���Ѵ�.
	 * msgFlag�� false�̸� ���� ����Ǿ� �ִ� ��Ʈ��ũ�� �˷��ش�.
	 * ��Ʈ��ũ�� ���� �Ұ��� ����� ���� ���̾�α�â�� ��� �˸���.
	 * @param msgFlag
	 * 		Toast �޼���  ��뿩��
	 * @return
	 *		��Ʈ��ũ ��밡�� ����
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
				Toast.makeText(PictureActivity.this, "Wi-Fi���� �������Դϴ�.",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (msgFlag == false) {
				Toast.makeText(PictureActivity.this, "3G���� �������Դϴ�.",
						Toast.LENGTH_SHORT).show();
			}
		}

		if (!isMobileConn && !isWifiConn) {
			/*
			 * ��Ʈ��ũ ������ ���� ������� ���� ȭ������ ���ư���.
			 */
			showDialog(NO_NETWORK_DIALOG);
			return false;
		}
		return true;

	}

	// ���� �� ���� ��ư ó��
	Button.OnClickListener mClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.attach_btn:
				// �ִ� 3���� ���ϸ� ������ �����ϰ� ��
				if (fileList.size() < MAX_ATTACH) {
					showDialog(FILE_CHOICE_DIALOG);
				} else { // �̹� 3���̸�
					new AlertDialog.Builder(PictureActivity.this)
							.setMessage("�ִ� " + MAX_ATTACH + "���� �̹����� ���� �����մϴ�.")
							.setTitle("�˸�").setCancelable(false)
							.setPositiveButton("Ȯ��", null).show();
				}
				break;
			// �Ű� ���� �κ�
			case R.id.send:
				/*
				 * ���̾�α׸� ���� ����� ��ġ ���� ���ο��� Ȯ��
				 */
				
				if( fileList.size() <= 0){	// ������ ������..
					Toast.makeText(PictureActivity.this, "���õ� ������ �����ϴ�.", Toast.LENGTH_SHORT).show();
					break;
				}

				new AlertDialog.Builder(PictureActivity.this)
						.setTitle("��ġ���� ���ο���")
						.setSingleChoiceItems(R.array.location_allow, -1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										allowLocation(which);
										dialog.dismiss();
									}
								}).setNegativeButton("���", null).show();
				break;

			}
		}

	};

	/**
	 * URI�� ���� ���� ���� ��θ� �����´�.
	 * 
	 * @param uriPath
	 *            URI : URI ���
	 * @return String : ���� ���� ���
	 */
	public String getRealImagePath(Uri uriPath) {
		String[] proj = { MediaStore.Images.Media.DATA };

		Cursor cursor = managedQuery(uriPath, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String path = cursor.getString(index);
		// path = path.substring(5);
		// �Ϻ� motorola���� ���� path�� '/mnt/' �� ������ ����..
		return path.replace("/mnt/", "");
	}

	
	/**
	 * �����Կ� Ȥ�� �������ù�ȣ�� �޾� �ش� intent�� �ѱ��.
	 * @param which
	 * 		
	 */
	public void attachFileFilter(int which) {
		Intent intent = null;
		intent = new Intent();
		switch (which) {
		case 0: // ���� �Կ��̸�

			// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(intent, TAKE_PICTURE);
			break;
		case 1: // ���� �����̸�
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
	 * android.content.Intent) ���� ���� ��Ƽ��Ƽ ��� ����
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		// ���� �Կ����ÿ� uri�� ���� ��� bitmap�� �̿��� ������ ����� �ش�.
		if (requestCode == TAKE_PICTURE && data.getData() == null) {
			String tmpFile = getDateTime() + ".jpg";
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			try {
				// bitmap�� jpg�� ����������
				bm.compress(CompressFormat.JPEG, 100,
						openFileOutput(tmpFile, MODE_PRIVATE));
			} catch (FileNotFoundException e) {
				new AlertDialog.Builder(PictureActivity.this)
						.setMessage("������ ������ �����ü��� �����ϴ�.").setTitle("�˸�")
						.setPositiveButton("Ȯ��", null).show();
			}

			selectedFile = PictureActivity.this.getFilesDir() /* �� ���ø����̼� ���� ��ġ */
					+ "/" + tmpFile;
		} else {
			selectedFile = getRealImagePath(data.getData());
		}
		// �ߺ� �������� üũ
		for (String matchFile : fileList) {
			if (matchFile.equals(selectedFile)) {
				new AlertDialog.Builder(PictureActivity.this)
						.setMessage("�̹� ���Ե� �����Դϴ�.").setTitle("�˸�")
						.setPositiveButton("Ȯ��", null).show();
				return;
			}
		}
		// ���� ���� üũ
		String file = selectedFile.substring(selectedFile.lastIndexOf("/") + 1);
		if (file.length() >= MAX_FILE_NAME_LENGTH) {
			new AlertDialog.Builder(PictureActivity.this)
					.setMessage("���ϸ��� �ʹ� ��ϴ�.\n�����̸��� 100�� �̳��� ���ּ���.")
					.setTitle("�˸�").setPositiveButton("Ȯ��", null).show();
			return;
		}

		File tmpfile = null;
		tmpfile = new File(selectedFile); // File ��ü ����
		int fileSize = (int) tmpfile.length(); // File ��ü�� length() �޼���� ���� ����
												// ���ϱ�
		if (fileSize > MAX_FILE_SIZE) {
			int size = MAX_FILE_SIZE / 1024 / 1024;
			new AlertDialog.Builder(PictureActivity.this)
					.setMessage(
							"���� �뷮�� " + String.valueOf(size) + "MB �̳��� ���ּ���")
					.setTitle("�˸�").setPositiveButton("Ȯ��", null).show();
			return;
		}

		fileList.add(selectedFile);
		// ����Ϳ��� ������ ������ �˸�
		adapter.notifyDataSetChanged();
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		super.onCreateDialog(id);
		switch (id) {
		case FILE_CHOICE_DIALOG:	// ���� ���� ���̾�α�
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(PictureActivity.this)
					.setTitle("÷������ ����")
					.setItems(R.array.attach,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// ÷������ ���� ó�� �޼ҵ�
									attachFileFilter(which);
								}
							}).setNegativeButton("���", null).show();

		case NO_NETWORK_DIALOG:	// ��Ʈ��ũ ���� ���� �˸�
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(this)
					.setTitle("�˸�")
					.setMessage(
							"Wifi Ȥ�� 3G���� ������� �ʾҰų� "
									+ "��Ȱ���� �ʽ��ϴ�.��Ʈ��ũ Ȯ���� �ٽ� ������ �ּ���!")
					.setPositiveButton("�ݱ�",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss(); // �ݱ�
									Intent intent = new Intent(
											PictureActivity.this,
											StartActivity.class);
									startActivity(intent);
									finish();
								}
							}).show();

		case CHECK_GPS_DIALOG:	// GPS Ȱ�� ���� ���̾�α�
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(this)
					.setMessage(
							" ��ġ ���� ��Ʈ��ũ ���  Ȥ�� GPS�� ��Ȱ��ȭ �Ǿ��ֽ��ϴ�. ����ȭ������ �̵� �Ͻðڽ��ϱ�?")
					.setCancelable(false)
					.setTitle("�˸�")
					.setPositiveButton("�̵�",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									moveConfigGPS();
								}
							})
					.setNegativeButton("���",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).show();

		case FAIL_CONNECT_DIALOG:	// ���� ���� ���� ���̾�α�
			if (getLastNonConfigurationInstance() != null) {
				return null;
			}
			return new AlertDialog.Builder(PictureActivity.this)
					.setMessage("�������� ������ ���� �߽��ϴ�.\n��Ʈ��ũ ���� üũ�� �ٽ� ������ �ּ���")
					.setTitle("�˸�")
					.setCancelable(false)
					.setPositiveButton("Ȯ��",
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
	 * gpsȤ�� network ��ġ������ �Ǿ������� ���̾�α� ����
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

	// ��ġ���� ���ο��� ���̾�α� �� ������ ó�� �Լ�
	private void allowLocation(int which) {

		double[] array = null;
		switch (which) {
		case 0:
			// gotoLocationSetting();
			array = getLocation(false); // ��ġ ���� ��������
			if (array == null) { // ��ġ ������ ���޾��� ���
				privacyAllow = "N";
				Toast.makeText(PictureActivity.this,
						"���� ��ġ�� ã���� ���� ��ġ���� �̽������� �߼� �˴ϴ�.", Toast.LENGTH_LONG)
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
			array = getLocation(false); // ��ġ ���� ��������
			if (array == null) { // ��ġ ������ ���޾��� ���
				Toast.makeText(PictureActivity.this, "���� ��ġ�� ã���� �����ϴ�.",
						Toast.LENGTH_LONG).show();
				break;
			} else {
				// ���� uri ���� �� zoom ����
				String geoURI = String.format("geo:%f,%f?z=%d", array[0],
						array[1], ZOOM_DEEP);
				Uri geo = Uri.parse(geoURI);
				Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
				startActivity(geoMap);
			}

		}
	}

	/**
	 * ���� ���ε� ���� ó��
	 * AsynTask�� �̿��Ͽ� thread�� ó���Ѵ�.
	 */
	private void fileUpload() {
		fileupload = new AsyncTaskFileUpload();
		mLockScreenRotation(); // ���ε��� ȭ�� ���� ó��
		fileupload.execute();
	}

	/**
	 * ��������ȯ�� ����
	 */
	private void clearPrefer() {
		// ���� ����
		SharedPreferences.Editor prefEditor = settings.edit();
		// ��ȭ��ȣ�� �״�� ��������
		String cellNum = settings.getString("cellNum", "");
		prefEditor.clear();
		prefEditor.putString("cellNum", cellNum);
		prefEditor.commit();

	}

	private void setPreferences() { // ���� ���� ��������
		/*
		 * String savedMessage = settings.getString("message", "");
		 * ((TextView)findViewById(R.id.reportMessage)).setText(savedMessage);
		 * //Toast.makeText(PictureActivity.this, savedMessage,
		 * Toast.LENGTH_LONG).show(); String filename; File f = null;
		 * if(settings.contains("file0") == true){ filename =
		 * settings.getString("file0", ""); f = new File(filename); if(
		 * f.isFile() ){ // ������ �����ϸ� ����Ʈ�� �־��� fileList.add( filename ); }
		 * 
		 * } if(settings.contains("file1") == true){ filename =
		 * settings.getString("file1", ""); f = new File(filename); if(
		 * f.isFile() ){ // ������ �����ϸ� ����Ʈ�� �־��� fileList.add( filename ); } }
		 * if(settings.contains("file2") == true){ filename =
		 * settings.getString("file2", ""); f = new File(filename); if(
		 * f.isFile() ){ // ������ �����ϸ� ����Ʈ�� �־��� fileList.add( filename ); } }
		 * 
		 * adapter.notifyDataSetChanged(); // ����Ϳ��� �˷���
		 */
	}


	// GPS ����ȭ������ �̵�
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
	 * ) ȭ�� ȸ���� �ؽ�Ʈ �Է� ������ ������� �ʰ�
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("files", fileList); // ���õ� ���� ����Ʈ ����
		return map;
	}

	@SuppressWarnings("unchecked")
	private void restore() {
		Object obj = getLastNonConfigurationInstance();
		if (obj != null) {
			// Map���·� �����߱⶧���� casting �ؼ� ����Ѵ�.
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
	 * ����� ��ġ�� ���� ��ǥ ������
	 */
	private double[] getLocation(boolean msgFlag) {

		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		/*
		 * Criteria criteria = new Criteria();
		 * criteria.setAccuracy(Criteria.ACCURACY_FINE);// ��Ȯ��
		 * criteria.setPowerRequirement(Criteria.POWER_HIGH); // ���� �Һ�
		 * criteria.setAltitudeRequired(false); // �� ��뿩��
		 * criteria.setBearingRequired(false); //
		 * criteria.setSpeedRequired(false); // �ӵ�
		 * criteria.setCostAllowed(true); // ���������
		 */
		// String provider = locationManager.getBestProvider(criteria, true);
		String provider;
		// gps �� ���� ������ gps�� ���� ����
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
			locationManager.requestLocationUpdates(provider, 100, 0,
					loclistener);// ���������� ������Ʈ
			location = locationManager.getLastKnownLocation(provider);
		} else { // ������ null
			location = null;
		}

		if (location == null) {

			// ���� ��ũ��Ʈ�� ���� ��ġ ������ �ȵǾ� ������ �׳� null ó��
			if (!(locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
				return null;
			}

			// ��Ʈ��ũ�� ��ġ�� ������
			provider = LocationManager.NETWORK_PROVIDER;
			// criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			// provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			locationManager.requestLocationUpdates(provider, 1000, 10,
					loclistener);
			if (msgFlag == false) {
				Toast.makeText(PictureActivity.this,
						"�ǳ��� �ְų� GPS�� �̿��Ҽ� ����  ��Ʈ��ũ�� ���� ������ġ�� ã���ϴ�.",
						Toast.LENGTH_LONG).show();
			}
			if (location == null) {
				return null;
			}
		}
		// ����, �浵 ��������
		double[] array = { location.getLatitude(), location.getLongitude() };
		return array;
	}

	// �޼��� ����
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (isFinishing() == true) { // ���α׷��� �������... ȭ�� ȸ���� ���� ����
			SharedPreferences setting = getSharedPreferences(PREFERENCE,
					MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = setting.edit();
			for (int i = 0; i < fileList.size(); i++) {
				prefEditor.putString("file" + i, fileList.get(i));
			}
			prefEditor.commit();
		}
	}

	// ��ġ ������ ó�� 
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
	 * ftp ���� ����
	 */
	private boolean connectFTP() {
		mFtp = new MyFTPClient(SERVER_IP, SERVER_FTP_PORT, FTP_NAME,
				FTP_PASSWORD);
		if (!mFtp.connect()) {
		//	Toast.makeText(PictureActivity.this,
		//			"�������� ����!\n��Ʈ��ũ ���� üũ ��  �ٽ� �õ��� �ּ���", Toast.LENGTH_SHORT)
		//			.show();
			return false;
		}

		if (!mFtp.login()) {
		//	Toast.makeText(PictureActivity.this, "�α��� ����!", Toast.LENGTH_SHORT)
		//			.show();
			mFtp.logout();
			return false;
		}
		
		mFtp.cd(FTP_PATH);
		return true;
	}

	/*
	 * ���� �̸��� �ٿ��� ����
	 */
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/*
	 * ���� ������ Ȯ���ڸ� ���ο� ���Ͽ� �ٿ���
	 */
	private String getExtension(String oldFile, String sep) {
		// Ȯ���� ��������
		int index = oldFile.lastIndexOf(sep);
		String ext = oldFile.substring(index).toLowerCase();
		return ext;
	}
	
	/**
	 * ȸ���� �ٽ� ȭ���� �ҷ����°� �����ϱ����� ��ũ���� �ᰡ�ش�. 
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
	 *	���� ���� �� ��ġ ���� ó�� Ŭ����
	 *
	 */
	private class AsyncTaskFileUpload extends
			AsyncTask<Object, String, Boolean> {
		ProgressDialog dialog = null;

		@Override
		protected void onPostExecute(Boolean result) {	// ���� �Ϸ���
			// ��� ������ ������ �Ϸ�Ǹ� ���̾�α׸� �ݴ´�.
			dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
			if( PictureActivity.this.mFtp.isConnected()){	// ������ �Ǿ� ������ 
				PictureActivity.this.mFtp.logout(); // �α� �ƿ�
			}
			// ���� ���� ����� ���
			if (result) { // ���� ������ �����̸�
				Intent intent = new Intent(PictureActivity.this,
						ResultActivity.class);
				intent.putExtra("message", PictureActivity.this.message);
				intent.putExtra("date", getDateTime());
				intent.putExtra("allow", PictureActivity.this.privacyAllow);
				startActivity(intent);
				PictureActivity.this.finish();

			} else {
				Toast.makeText(PictureActivity.this, "���� ���� ����!\n ��Ʈ��ũ ���� �� �������¸� üũ�ϼ���",
						Toast.LENGTH_LONG).show();
				// ���� ���� ó�� �ؾߵ�
			}

			PictureActivity.this
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // ȭ��
																							// ����
																							// ����
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute() ���� ������ �ε��� ��Ÿ����
		 */
		@Override
		protected void onPreExecute() {	// ������ ���α׷��� ���̾�α׷� ���������� ����ڿ��� �˸���.
			dialog = ProgressDialog.show(PictureActivity.this, "������",
					"����� ȯ�濡 ���� ���� �ӵ��� �ٸ��� �ֽ��ϴ�." + " ��� ��ٷ��ּ���", true);
			 dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[]) �񵿱� ���� ����
		 */
		@Override
		protected Boolean doInBackground(Object... params) {	// ������

			// TODO Auto-generated method stub
			boolean result = true;

			if (!PictureActivity.this.checkNetWork(true)) { // ��Ʈ��ũ ���� üũ
				return false;
			}

			if (!PictureActivity.this.connectFTP()) { // ftp ������ �ȵǸ�
				return false;
			}
			
			if (!MyUtils.checkVersion("photoclude", "1.3")) {
				finish();
			}			
			
			// http �� ���� �̸� �� �� �÷���
			Vector<NameValuePair> vars = new Vector<NameValuePair>();
			DeviceInfo di = DeviceInfo
					.setDeviceInfo((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));	// ����̽� ���� ��
			try {
				/* ���� ���ε� */
				for (int i = 0; i < PictureActivity.this.fileList.size(); i++) {
					String tmpFile = PictureActivity.this.fileList.get(i);
					storeFiles[i] = tmpFile
							.substring(tmpFile.lastIndexOf("/") + 1); // ���� ���ϸ�	 ������
					// �̸� �ٲٱ� yyyymmdd_hhmmss_Cellnum_01.xxx
					receiveFiles[i] = getDateTime() + "_" + di.getDeviceNumber() + i + getExtension(tmpFile, ".");
					Log.d(DEBUG_TAG, storeFiles[i]);
					Log.d(DEBUG_TAG, "size" + i);
					// ���� ���ε�
					if (!PictureActivity.this.mFtp.upload(tmpFile,
							receiveFiles[i])) {
						// Toast.LENGTH_SHORT).show();
						result = false;
					} else {
						 int j = i +1;
						 vars.add(new BasicNameValuePair("filename" + j, receiveFiles[i]));	// �����̸�
					}
				}
				
				// �޼��� üũ
				String message ="";
				if(!TextUtils.isEmpty(mMessage.getText().toString())){	// �޼��� ������ ������ �޽����� �־��ش�.
					message = new String(mMessage.getText().toString().getBytes("UTF-8"));
				}
				
				SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(PictureActivity.this);
				String tag = defaultSharedPref.getString("tag_filter", "��ü");	
				
				// HTTP GET �޼��带 �̿��Ͽ� ������ ���ε� ó��
				vars.add(new BasicNameValuePair("lat", latitude));	// ����
	            vars.add(new BasicNameValuePair("lon", longitude));	// �浵
	            vars.add(new BasicNameValuePair("phone", di.getDeviceNumber()));	
	            vars.add(new BasicNameValuePair("device_id", di.getMyDeviceID()));	
	            vars.add(new BasicNameValuePair("tag", tag));	// �±�            
	            vars.add(new BasicNameValuePair("message", message));	// �޼���
	            
	            String url = "http://" + SERVER_IP + "/pic_project/insert.php"; 
	           //+ URLEncodedUtils.format(vars, null);
	            
	            HttpPost request = new HttpPost(url);	
	            request.setEntity(new UrlEncodedFormEntity(vars, "UTF-8"));
	            try {

	                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                HttpClient client = new DefaultHttpClient();
	                String responseBody = client.execute(request, responseHandler);	// ����

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
				dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
				Log.e(DEBUG_TAG, "���� ���ε� ����", e);
			}

			return result;
		}

	}
	

	@Override
	public void onBackPressed() {	//  �ڷ� �����ư Ŭ���� ���� ����
		// TODO Auto-generated method stub
		//super.onBackPressed();
		finishDialog(this);
		
	}	

}
