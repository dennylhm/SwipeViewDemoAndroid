package com.zdu.swipeviewdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zdu.swipeviewdemo.SwipeView.OnSwipeChangeListener;

public class MainActivity extends Activity {

	private ListView mListView;

	private ArrayList<String> mData;

	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.listView);
		initData();

		adapter = new MyAdapter();
		mListView.setAdapter(adapter);

	}

	/**
	 * 初始化假数据
	 */
	private void initData() {
		mData = new ArrayList<String>();
		for (int i = 0; i < 50; i++) {
			mData.add(i + "");
		}
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(MainActivity.this, R.layout.swipeview, null);
				holder.content = (TextView) convertView.findViewById(R.id.content);
				holder.swipe = (TextView) convertView.findViewById(R.id.menu);
				holder.swipe.setOnClickListener(mOnClick);
				holder.swipeView = (SwipeView) convertView;
				// 设置左滑删掉的点击监听
				holder.swipeView.setOnSwipeChangeListener(mOnSwipe);
				convertView.setTag(holder);//setTag和getTag是绑定和被绑定的意思
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.content.setText(mData.get(position));
			holder.swipe.setText("删除");
			//保存下标
			holder.swipe.setTag(position);
			return convertView;
		}
	}

	/**
	 * 左滑删掉
	 */
	private SwipeView openedSwipeView;
	/**
	 * 左滑删除监听
	 */
	private OnSwipeChangeListener mOnSwipe = new OnSwipeChangeListener() {

		@Override
		public void onOpen(SwipeView swipeView) {
			// 保存swipView
			openedSwipeView = swipeView;
		}

		@Override
		public void onDown(SwipeView swipeView) {
			if (openedSwipeView != null && openedSwipeView != swipeView) {
				openedSwipeView.close();
			}
		}

		@Override
		public void onClose(SwipeView swipeView) {
			if (openedSwipeView != null && openedSwipeView == swipeView) {
				openedSwipeView = null;
			}
		}
	};
	/**
	 * 删除点击监听
	 */
	private OnClickListener mOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			//取出保存的下标
			int position = (Integer) v.getTag();
			//Log.e("TAG", position + "");
			mData.remove(position);
			adapter.notifyDataSetChanged();
			Toast.makeText(MainActivity.this, "删除数据", Toast.LENGTH_SHORT).show();
		}
	};

	static class ViewHolder {
		TextView content;
		TextView swipe;
		SwipeView swipeView;
	}
}
