package nl.ict.dfinger;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ObjectPathAdapter extends BaseAdapter
{
	private List<Object> list = Collections.emptyList();
	
	protected LayoutInflater invlater;
	
	public ObjectPathAdapter(Context context) {
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
		// TODO Auto-generated method stub
		return null;
	}

}
