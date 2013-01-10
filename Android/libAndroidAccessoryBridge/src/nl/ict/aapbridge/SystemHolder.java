package nl.ict.aapbridge;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * 
 */

/**
 * @author Jurgen Reintjes
 * holds global system variables 
 */
public class SystemHolder {
	//hold the current id for the messageid
	public static int mId = 200000;
	private static Intent mIntent;
	private static Context mContext;
	private static Activity mActivity;
	
	/**
	 * @return the mActivity
	 */
	public static Activity getActivity() {
		return mActivity;
	}

	/**
	 * @param mActivity the mActivity to set
	 */
	public static void setActivity(Activity mActivity) {
		SystemHolder.mActivity = mActivity;
	}

	private SystemHolder() { }

	/**
	 * @return the mIntent
	 */
	public static Intent getIntent() {
		return mIntent;
	}

	/**
	 * @param mIntent the mIntent to set
	 */
	public static void setIntent(Intent intent) {
		SystemHolder.mIntent = intent;
	}

	/**
	 * @return the mContext
	 */
	public static Context getContext() {
		return mContext;
	}

	/**
	 * @param mContext the mContext to set
	 */
	public static void setContext(Context context) {
		SystemHolder.mContext = context;
	}
	
	
	
}
