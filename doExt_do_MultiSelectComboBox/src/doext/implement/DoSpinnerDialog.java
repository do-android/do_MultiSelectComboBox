package doext.implement;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DoSpinnerDialog extends Dialog implements OnClickListener, OnCancelListener {

	private ListView listView;
	private HashMap<Integer, Boolean> itemStatus;

	private HashMap<Integer, Boolean> tempItemStatus;

	private OnClickCancelListener listener;

	public interface OnClickCancelListener {
		void clickCancel(HashMap<Integer, Boolean> itemStatus);
	}

	public void setOnClickCancelListener(OnClickCancelListener _listener) {
		this.listener = _listener;
	}

	public DoSpinnerDialog(Context context, HashMap<Integer, Boolean> _itemStatus) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.itemStatus = _itemStatus;
		tempItemStatus = new HashMap<Integer, Boolean>();

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		TextView textView = new TextView(context);
		textView.setPadding(0, px2dip(getContext(), 80), 0, px2dip(getContext(), 50));
		textView.setText("完成");
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		textView.setTextColor(Color.BLACK);
		textView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
		lp.gravity = Gravity.CENTER;
		layout.addView(textView, lp);
		textView.setOnClickListener(this);

		listView = new ListView(context);
		listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		layout.addView(listView);
		setContentView(layout);

		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.gravity = Gravity.CENTER;
		DisplayMetrics dm = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		params.width = (int) (screenWidth * 0.8);
		window.setAttributes(params);
		this.setOnCancelListener(this);
	}

	public void setAdapter(ListAdapter adapter) {
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Boolean isCheck = tempItemStatus.get(position);
				if (isCheck == null) {
					tempItemStatus.put(position, !itemStatus.get(position));
				} else {
					tempItemStatus.put(position, !tempItemStatus.get(position));
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		this.cancel();
		if (listener != null) {
			for (Entry<Integer, Boolean> _entry : tempItemStatus.entrySet()) {
				itemStatus.put(_entry.getKey(), _entry.getValue());
			}
			tempItemStatus.clear();
			listener.clickCancel(itemStatus);
		}
	}

	private int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public void setSelection(HashMap<Integer, Boolean> itemStatus) {
		for (Entry<Integer, Boolean> _entry : itemStatus.entrySet()) {
			listView.setItemChecked(_entry.getKey(), _entry.getValue());
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (tempItemStatus != null) {
			tempItemStatus.clear();
		}
	}
	
	public void setBgColor(int color){
		if(listView != null){
			listView.setBackgroundColor(color);
		}
	}
}
