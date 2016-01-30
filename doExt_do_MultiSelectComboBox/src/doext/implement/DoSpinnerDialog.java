package doext.implement;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
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
	private SparseBooleanArray itemStatus;

	private SparseBooleanArray tempItemStatus;

	private OnClickCancelListener listener;

	public interface OnClickCancelListener {
		void clickCancel(SparseBooleanArray itemStatus);
	}

	public void setOnClickCancelListener(OnClickCancelListener _listener) {
		this.listener = _listener;
	}

	public DoSpinnerDialog(Context context, SparseBooleanArray _itemStatus) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.itemStatus = _itemStatus;
		tempItemStatus = new SparseBooleanArray();

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		TextView textView = new TextView(context);
		textView.setPadding(0, 15, 0, 10);
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
				if (tempItemStatus.indexOfKey(position) < 0) {
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
			for (int i = 0; i < tempItemStatus.size(); i++) {
				int _key = tempItemStatus.keyAt(i);
				itemStatus.put(_key, tempItemStatus.get(_key));
			}
			tempItemStatus.clear();
			listener.clickCancel(itemStatus);
		}
	}

	public void setSelection(SparseBooleanArray _itemStatus, boolean _isFire) {
		for (int i = 0; i < _itemStatus.size(); i++) {
			int _key = _itemStatus.keyAt(i);
			listView.setItemChecked(_key, _itemStatus.get(_key));
		}
		if (listener != null && _isFire) {
			listener.clickCancel(_itemStatus);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (tempItemStatus != null) {
			tempItemStatus.clear();
		}
	}

	public void setBgColor(int color) {
		if (listView != null) {
			listView.setBackgroundColor(color);
		}
	}
}
