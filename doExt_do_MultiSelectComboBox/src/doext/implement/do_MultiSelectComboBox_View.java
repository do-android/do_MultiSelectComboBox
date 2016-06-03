package doext.implement;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoResourcesHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIListData;
import core.interfaces.DoIModuleTypeID;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;
import core.object.DoProperty;
import core.object.DoUIModule;
import doext.define.do_MultiSelectComboBox_IMethod;
import doext.define.do_MultiSelectComboBox_MAbstract;
import doext.implement.DoSpinnerDialog.OnClickCancelListener;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,
 * do_MultiSelectComboBox_IMethod接口； #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_MultiSelectComboBox_View extends RelativeLayout implements DoIUIModuleView, do_MultiSelectComboBox_IMethod, OnClickCancelListener, OnClickListener, DoIModuleTypeID {

	private ArrayAdapter<String> mAdapter;
	private String fontStyle;
	private String fontColor;
	private String fontSize;
	private String textFlag;
	private String textAlign = "left";

	private SparseBooleanArray itemStatus;

	private DoSpinnerDialog spinnerDialog;

	private Context context;

	private TextView mTextView;
	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_MultiSelectComboBox_MAbstract model;

	public do_MultiSelectComboBox_View(Context context) {
		super(context);
		this.context = context;
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_MultiSelectComboBox_MAbstract) _doUIModule;

		mTextView = new TextView(context);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(_doUIModule, "17"));
		mTextView.setTextColor(Color.BLACK);
		RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(-1, -2);
		tvParams.leftMargin = 5;
		tvParams.addRule(CENTER_VERTICAL);
		this.addView(mTextView, tvParams);

		View rigthView = new View(context);
		tvParams = new RelativeLayout.LayoutParams((int) (_doUIModule.getRealHeight() / 3), (int) (_doUIModule.getRealHeight() / 3));
		tvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		tvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		int do_multiselect_combobox_flag_id = DoResourcesHelper.getIdentifier("do_multiselect_combobox_flag", "drawable", this);
		rigthView.setBackgroundResource(do_multiselect_combobox_flag_id);
		this.addView(rigthView, tvParams);

		View lineView = new View(context);
		lineView.setBackgroundColor(Color.GRAY);
		tvParams = new RelativeLayout.LayoutParams(-1, 2);
		tvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		this.addView(lineView, tvParams);

		itemStatus = new SparseBooleanArray();
		spinnerDialog = new DoSpinnerDialog(context, itemStatus);
		spinnerDialog.setOnClickCancelListener(this);
		this.setOnClickListener(this);
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		DoUIModuleHelper.setFontProperty(this.model, mTextView, _changedValues);

		if (_changedValues.containsKey("textAlign")) {
			this.textAlign = _changedValues.get("textAlign");
		}

		if (_changedValues.containsKey("fontStyle")) {
			this.fontStyle = _changedValues.get("fontStyle");
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

		if (_changedValues.containsKey("fontColor")) {
			this.fontColor = _changedValues.get("fontColor");
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

		if (_changedValues.containsKey("textFlag")) {
			this.textFlag = _changedValues.get("textFlag");
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

		if (_changedValues.containsKey("fontSize")) {
			this.fontSize = _changedValues.get("fontSize");
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

		if (_changedValues.containsKey("items")) {
			String _items = _changedValues.get("items");
			String[] _data = new String[0];
			if (!TextUtils.isEmpty(_items)) {
				_data = _items.split(",");
			}
			mAdapter = new MyAdapter(this.getContext(), android.R.layout.simple_list_item_multiple_choice, _data);
			spinnerDialog.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
			try {
				setSelection(model.getPropertyValue("indexs"));
			} catch (Exception _err) {
				DoServiceContainer.getLogEngine().writeError(model.getTypeID() + " indexs \n\t", _err);
			}
		}

		if (_changedValues.containsKey("indexs")) {
			setSelection(_changedValues.get("indexs"));
		}
	}

	private void setSelection(String _indexStr) {
		if (itemStatus != null && itemStatus.size() > 0) {
			for (int i = 0; i < itemStatus.size(); i++) {
				itemStatus.put(i, false);
			}
		}

		String[] _indexs = _indexStr.split(",");
		if (_indexs != null && _indexs.length > 0) {
			for (int i = 0; i < _indexs.length; i++) {
				int _index = DoTextHelper.strToInt(_indexs[i], -1);
				if (mAdapter != null && _index >= 0 && _index < mAdapter.getCount()) {
					itemStatus.put(_index, true);
				}
			}
			if (mAdapter != null) {
				spinnerDialog.setSelection(itemStatus, true);
			}
		}

	}

	private class MyAdapter extends ArrayAdapter<String> {
		public MyAdapter(Context context, int textViewResourceId, String[] objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView _tv = (TextView) super.getView(position, convertView, parent);
			setTextViewStyle(_tv);
			return _tv;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			TextView _tv = (TextView) super.getDropDownView(position, convertView, parent);
			setTextViewStyle(_tv);
			return _tv;
		}
	}

	private void setTextViewStyle(TextView _tv) {
		DoUIModuleHelper.setTextFlag(_tv, textFlag);
		DoUIModuleHelper.setFontStyle(_tv, fontStyle);
		if (this.textAlign.equals("center")) {
			_tv.setGravity(Gravity.CENTER);
		} else if (this.textAlign.equals("right")) {
			_tv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		} else {
			_tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		}
		_tv.setTextColor(DoUIModuleHelper.getColorFromString(fontColor, Color.BLACK));
		_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(model, fontSize));
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("bindItems".equals(_methodName)) {
			bindItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("refreshItems".equals(_methodName)) {
			refreshItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	private void bindItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _address = DoJsonHelper.getString(_dictParas, "data", "");
		if (_address == null || _address.length() <= 0)
			throw new Exception("do_MultiSelectComboBox_View 未指定相关的listview data参数！");
		DoMultitonModule _multitonModule = DoScriptEngineHelper.parseMultitonModule(_scriptEngine, _address);
		if (_multitonModule == null)
			throw new Exception("do_MultiSelectComboBox_View data参数无效！");
		if (_multitonModule instanceof DoIListData) {
			DoIListData _data = (DoIListData) _multitonModule;
			int _count = _data.getCount();
			String[] _newData = new String[_count];
			for (int i = 0; i < _count; i++) {
				Object _childData = _data.getData(i);
				if (_childData instanceof JSONObject) {
					_newData[i] = DoJsonHelper.getString((JSONObject) _childData, "text", "");
				} else {
					_newData[i] = _childData.toString();
				}
			}

			mAdapter = new MyAdapter(this.getContext(), android.R.layout.simple_list_item_multiple_choice, _newData);
			spinnerDialog.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
			try {
				setSelection(model.getPropertyValue("indexs"));
			} catch (Exception _err) {
				DoServiceContainer.getLogEngine().writeError(model.getTypeID() + " indexs \n\t", _err);
			}
		}
	}

	private void refreshItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) {
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		// ...do something
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		// ...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	@Override
	public void clickCancel(SparseBooleanArray _itemStatus) {
		DoInvokeResult _result = new DoInvokeResult(model.getUniqueKey());
		JSONArray _array = new JSONArray();
		StringBuffer _sb = new StringBuffer();
		for (int i = 0; i < itemStatus.size(); i++) {
			int _key = itemStatus.keyAt(i);
			boolean _value = itemStatus.get(_key);
			if (_value) {
				_sb.append(_key + ",");
				_array.put(_key);
			}
		}
		_result.setResultArray(_array);
		if (_sb.length() > 0) {
			this.model.setPropertyValue("indexs", _sb.substring(0, _sb.length() - 1));
		} else {
			this.model.setPropertyValue("indexs", "");
		}
		model.getEventCenter().fireEvent("selectChanged", _result);
	}

	@Override
	public void onClick(View v) {
		if (spinnerDialog != null) {
			spinnerDialog.show();
			spinnerDialog.setSelection(itemStatus, false);
			try {
				DoProperty _bgColor = model.getProperty("bgColor");
				if (_bgColor != null) {
					spinnerDialog.setBgColor(DoUIModuleHelper.getColorFromString(_bgColor.getValue(), Color.WHITE));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getTypeID() {
		return model.getTypeID();
	}

}