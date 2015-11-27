package com.zdu.swipeviewdemo;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 自定义左滑删除的视图
 * @author zdu
 *
 */
/*
1. 如何得到contentView和menuView对象?
       1). 重写onAttachedToWindow()
       2). 调用viewGroup.getChildAt(index)
2. 如何得到contentView和menuView的宽高?
       1). 重写onMeasure()
       2). 调用getMeasuredWidth()和getMeasuredHeight()
3. 如何给contentView和menuView进行初始化布局定位?
       1). 重写onLayout()
       2). 调用layout()对contentView与menuView进行布局定位
4. 如何拖动contentView与menuView?
       1). 使用ViewDragHelper这个视图拖拽的帮助类
       2). 创建ViewDragHelper对象, 并指定其回调对象, 并重写其中的一些方法
            boolean tryCaptureView(View child): 判断child是否是需要拖拽的子View, down时调用
            int clampViewPositionHorizontal(): 限制view在水平方向真正移动的距离, move时调用
            onViewPositionChanged(View child): 当子view的位置发生改变时回调, 需要在其中去移动其它的子View
            onViewReleased(View releasedChild): 当子View被释放时回调, 即up时调用 
            int getViewHorizontalDragRange(View child): 返回子View在水平方向最大移到的距离, 此方法在用于ListView时才需要重写
      3). 重写onTouchEvent(), 在其中让dragHelper对象来处理event对象
5. 如何解决手指滑动时ListView垂直方向滚动和SwipeView水平滑动的冲突问题?
       1). 在move事件时, 获取x轴和y轴方向的移动距离distanceX, distanceY
       2). 如果distanceX>distanceY, 执行requestDisallowInterceptTouchEvent(true), 使ListView不能拦截event
6. 如何解决SwipeView滑动与ContentView/menuView点击事件的冲突问题?
       1). 重写onInterceptTouchEvent()
       2). 返回dragHelper.shouldInterceptTouchEvent(ev), 由dragHelper来决定是否拦截event
7. 如何只让ListView中只有一个SwipeView能被打开?
       1). 为SwipeView定义枚举类型状态类
       2). 在SwipeView中定义监听接口,在接口中定义回调方法
       3). 定义设置监听接口对象的方法
       4). 在事件发生时, 调用监听对象处理
       5). 在Activity的Adapter中为每个SwipeView对象设置监听器对象, 在回调方法中做处理
8. 如何让swipeView的自动打开和关闭是平滑的效果?
       1). 调用下面的方法打开/关闭 layout()
            dragHelper.smoothSlideViewTo(contentView, -deleteWidth, 0);
            ViewCompat.postInvalidateOnAnimation(this);
      2). 重写computeScroll()
          if (dragHelper.continueSettling(true)) {
               ViewCompat.postInvalidateOnAnimation(this);
          }                 
9. ListView滑动时, 关闭打开的SwipeView   
       在ListView的Scroll监听中, 一旦滑动, 关闭打开的swipeView
 */
public class SwipeView extends FrameLayout {

	public SwipeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		dragHelper = ViewDragHelper.create(this, callBack);
//		 mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback()); 1.0f为敏感参数，越大越敏感
	}

	//1. 如何得到contentView和menuView对象?
	private View contentView;
	private View menuView;

	//在此方法执行时, 视图都创建好了
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		contentView = this.getChildAt(0);
		menuView = this.getChildAt(1);
	}

	//2. 如何得到contentView和menuView的宽高?
	private int contentWidth, menuWidth, viewHeight;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		contentWidth = contentView.getMeasuredWidth();
		menuWidth = menuView.getMeasuredWidth();
		viewHeight = contentView.getMeasuredHeight();
	}

	/*
	 3. 如何给contentView和menuView进行初始化布局定位?
	   1). 重写onLayout()
	   2). 调用layout()对contentView与menuView进行布局定位
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//使用menuView定位在contentView右侧
		menuView.layout(contentWidth, 0, contentWidth + menuWidth, viewHeight);
	}

	/*
	 4. 如何拖动contentView与menuView?
	   1). 使用ViewDragHelper这个视图拖拽的帮助类
	   2). 创建ViewDragHelper对象, 并指定其回调对象, 并重写其中的一些方法
	        boolean tryCaptureView(View child): 判断child是否是需要拖拽的子View, down时调用
	        int clampViewPositionHorizontal(): 限制view在水平方向真正移动的距离, move时调用
	        onViewPositionChanged(View child): 当子view的位置发生改变时回调, 需要在其中去移动其它的子View
	        onViewReleased(View releasedChild): 当子View被释放时回调, 即up时调用 
	        int getViewHorizontalDragRange(View child): 返回子View在水平方向最大移到的距离, 此方法在用于ListView时才需要重写
	  3). 重写onTouchEvent(), 在其中让dragHelper对象来处理event对象
	 */
	private ViewDragHelper dragHelper;
	private Callback callBack = new ViewDragHelper.Callback() {

		//判断child是否是需要拖拽的子View, true时调用
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			//Log.e("TAG", "tryCaptureView()");
			return child == contentView || child == menuView;
		}

		//限制view在水平方向真正移动的距离, move时调用
		/**
		 * child : contentView或者menuView
		 * left : 相对于down时的水平移动距离
		 * dx : 相对于上一个Move的距离
		 * 
		 * 返回移动的总距离
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			//Log.e("TAG", "clampViewPositionHorizontal() left=" + left + " dx=" + dx);
			if (child == contentView) {
				if (left < -menuWidth) {
					left = -menuWidth;
				} else if (left > 0) {
					left = 0;
				}
			} else if (child == menuView) {
				if (left < contentWidth - menuWidth) {
					left = contentWidth - menuWidth;
				} else if (left > contentWidth) {
					left = contentWidth;
				}
			}
			return left;
		}

		/**
		 * 当子view的位置发生改变时回调, 需要在其中去移动其它的子View
		 * changedView: 被拖动的view
		 * 
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			//Log.e("TAG", "onViewPositionChanged() left=" + left + " dx=" + dx);
			if (changedView == contentView) {
				menuView.layout(menuView.getLeft() + dx, 0, menuView.getRight() + dx, viewHeight);
			} else {//拖动就menuView
				contentView.layout(contentView.getLeft() + dx, 0, contentView.getRight() + dx, viewHeight);
			}
		}

		/**
		 * 当子View被释放时回调, 即up时调用 
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			//Log.e("TAG", "onViewReleased()");
			if (contentView.getLeft() < -menuWidth / 2) {
				//打开
				open();
			} else {
				//关闭
				close();
			}
		}

		/**
		 * 返回子View在水平方向最大移到的距离, 此方法在用于ListView时才需要重写
		 */
		@Override
		public int getViewHorizontalDragRange(View child) {
			//Log.e("TAG", "getViewHorizontalDragRange()");
			return menuWidth;
		}

	};

	/**
	5. 如何解决手指滑动时ListView垂直方向滚动和SwipeView水平滑动的冲突问题?
	   1). 在move事件时, 获取x轴和y轴方向的移动距离distanceX, distanceY
	   2). 如果distanceX>distanceY, 执行requestDisallowInterceptTouchEvent(true), 使ListView不能拦截event
	 */
	private int lastX;
	private int lastY;

	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			//Log.e("TAG", lastX + "");
			//当按下的坐标小于某一个值的时候，拦截此事件
			if (lastX <= 60) {
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) event.getRawX();
			int moveY = (int) event.getRawY();
			int distanceX = Math.abs(moveX - lastX);
			int distanceY = Math.abs(moveY - lastY);
			//如果向水平方向移动
			if (distanceX > distanceY) {
				//对ListView进行反拦截, 不让ListView拦截事件, 事件就会交给SwipeView处理
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;

		default:
			break;
		}

		//在其中让dragHelper对象来处理event对象
		//		if (canSwipe) //只有标识能打开, 才将事件交给helper处理, 这样才有可能打开
		dragHelper.processTouchEvent(event);
		return true;//消费事件
	}

	/**
	 * 关闭
	 */
	public void close() {
		//contentView.layout(0, 0, contentWidth, viewHeight);
		//menuView.layout(contentWidth, 0, contentWidth+menuWidth, viewHeight);
		dragHelper.smoothSlideViewTo(contentView, 0, 0);//将指定视图平滑移动到指定的坐标
		ViewCompat.postInvalidateOnAnimation(this);//能动画的方式强制重绘  //启动起来

		//通知监听器
		if (onSwipeChangeListener != null) {
			onSwipeChangeListener.onClose(this);
		}
	}

	/**
	 * 打开
	 */
	protected void open() {
		/*
		contentView.layout(-menuWidth, 0, contentWidth-menuWidth, viewHeight);
		menuView.layout(contentWidth-menuWidth, 0, contentWidth, viewHeight);
		*/

		dragHelper.smoothSlideViewTo(contentView, -menuWidth, 0);//将指定视图平滑移动到指定的坐标
		ViewCompat.postInvalidateOnAnimation(this);//能动画的方式强制重绘  //启动起来

		//通知监听器
		if (onSwipeChangeListener != null) {
			onSwipeChangeListener.onOpen(this);
		}
	}

	//draw()会调用此方法
	@Override
	public void computeScroll() {
		//Log.w("TAG", "computeScroll()");
		if (dragHelper.continueSettling(true)) {//判断视图是否已经移动到指定的位置, 返回true代表还没有完成
			ViewCompat.postInvalidateOnAnimation(this);//以动画的方式强制重绘, 导致draw()执行-->computeScroll()
		}
	}

	//private boolean canSwipe = true; //默认能滑动

	/**
	 6. 如何解决SwipeView滑动与ContentView/menuView点击事件的冲突问题?
	   1). 重写onInterceptTouchEvent()
	   2). 返回dragHelper.shouldInterceptTouchEvent(ev), 由dragHelper来决定是否拦截event
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (onSwipeChangeListener != null) {
				onSwipeChangeListener.onDown(this);
			}

		}

		boolean intercept = dragHelper.shouldInterceptTouchEvent(ev);
		//Log.i("TAG", "onInterceptTouchEvent() " + intercept);
		return intercept;
	}

	/**
	 * 自定义滑动监听，用于滑动删除，开启滑动和关闭滑动等操作
	 */
	private OnSwipeChangeListener onSwipeChangeListener;

	public void setOnSwipeChangeListener(OnSwipeChangeListener onSwipeChangeListener) {
		this.onSwipeChangeListener = onSwipeChangeListener;
	}

	public interface OnSwipeChangeListener {
		//打开
		public void onOpen(SwipeView swipeView);

		//关闭
		public void onClose(SwipeView swipeView);

		//按下
		//返回值表明当前这次操作能否滑动swipeView, true代表能滑动
		public void onDown(SwipeView swipeView);
	}
}
