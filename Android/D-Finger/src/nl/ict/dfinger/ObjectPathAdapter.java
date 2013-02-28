package nl.ict.dfinger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.ict.aapbridge.dbus.introspection.ObjectPath;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ObjectPathAdapter extends BaseAdapter
{
	private List<ObjectPath> list = new ArrayList<ObjectPath>();
	
	protected LayoutInflater invlater;
	
	public ObjectPathAdapter(Context context) {
		this.invlater = LayoutInflater.from( context );
	}
	
	public int getCount() {
		return list.size();
	}

	public ObjectPath getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	public void add(ObjectPath obj) {
		list.add(obj);
		this.notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = this.invlater.inflate( R.layout.activity_object_path_list_row, parent, false);
		((TextView)v.findViewById(R.id.objectpathlistactivity_row_name)).setText(list.get(position).toString());
		return v;
	}

}
