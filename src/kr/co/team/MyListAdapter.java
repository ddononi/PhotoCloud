package kr.co.team;


import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * 리스트 처리 Adapter
 *
 */
class MyListAdapter extends BaseAdapter{
	Context maincontext;
	ArrayList<String> filenames;
	LayoutInflater inf;	
	int layout;
	public MyListAdapter(Context context, int alayout, ArrayList<String> afilenames){
		maincontext = context;
		layout = alayout;
		
		// 레이아웃 인플레이터 가져오기
		inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		filenames = afilenames;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return filenames.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return filenames.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final int pos = position;
		if( convertView == null ){
			convertView = inf.inflate(layout, parent, false);
		}
		
		TextView tv =(TextView)convertView.findViewById(R.id.list_content);
		tv.setText(getItem(pos));
		/*
		 * 첨부파일명을 선택 하면 해당 파일을 보여준다.
		 */
		tv.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String uriStr = "file:///" + getItem(pos);
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
				//maincontext.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) ) );
				// 이미지 파일 확장만 필터해서 처리
				if( uriStr.toLowerCase().endsWith(".jpg") || uriStr.toLowerCase().endsWith(".png")  || uriStr.toLowerCase().endsWith(".jpeg") 
						|| uriStr.toLowerCase().endsWith(".bmp")  || uriStr.toLowerCase().endsWith(".gif")  ){
					intent.setDataAndType(Uri.parse(uriStr), "image/*");
				}else {
						//
				}
				
				maincontext.startActivity(intent);				
			}
			
		});
		
		// 삭제 버튼 클릭시 해당 항목을 없애주고 리스트 갱신
		convertView.findViewById(R.id.delBtn).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				filenames.remove(pos);	// 해당 항목 삭제
				MyListAdapter.this.notifyDataSetChanged();
				//String str = filenames.get(pos).toString();
				//Toast.makeText(maincontext, str, Toast.LENGTH_LONG).show();
			}
			
		});
	
		return convertView;
	}
	
	@Override
	public void notifyDataSetChanged (){
		super.notifyDataSetChanged ();
	}

}