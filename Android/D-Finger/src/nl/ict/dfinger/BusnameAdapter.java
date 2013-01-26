package nl.ict.dfinger;

import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BusnameAdapter extends BaseAdapter {
	
	private List<Object> list = Collections.emptyList();
	
	protected LayoutInflater invlater;
	
	public BusnameAdapter(Context context) {
		this.invlater = LayoutInflater.from( context );
	}
	
	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = this.invlater.inflate( R.layout.busnamelistactivity_row, parent, false);
		((TextView)v.findViewById(R.id.busnamelistactivity_row_name)).setText(list.get(position).toString());
		return v;
	}
	
	public void setList(List<Object> list)
	{
		this.list = list;
		notifyDataSetChanged();
	}
	
}
