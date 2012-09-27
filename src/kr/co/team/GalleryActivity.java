package kr.co.team;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 *	� ��� ��Ƽ��Ƽ
 */
public class GalleryActivity extends BaseActivity {
	private WebView mWebView;
	private ProgressDialog dialog = null;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_view_layout);
		initLayout();
	}
	
	/*
	 * Layout
	 */
	private void initLayout() {
		// ���� ����
		mWebView = (WebView) findViewById(R.id.webview);
		// ���� ���� ��������
		WebSettings ws = mWebView.getSettings();
		// ���信�� �ڹٽ�ũ��Ʈ���డ��
		ws.setJavaScriptEnabled(true);
		ws.setSupportZoom(false);
		ws.setBuiltInZoomControls(true);			// zoom ��Ʈ��
		ws.setPluginsEnabled(true);
		mWebView.setWebViewClient(new WebViewClientClass());		
		SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String tag = defaultSharedPref.getString("tag_filter", "��ü");		
		mWebView.loadUrl("http://ddononi.cafe24.com/pic_project/photoswipe/index.html?tag=" + tag);
	}		
	
	
	
	private class WebViewClientClass extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(final WebView view,
				final String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(final WebView view, final String url) {
			super.onPageFinished(view, url);

			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		}

		@Override
		public void onPageStarted(final WebView view, final String url,
				final Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			dialog = ProgressDialog.show(GalleryActivity.this, "�ε���",
					"������ �ҷ��������Դϴ�.");
		}

	}	

	
}
