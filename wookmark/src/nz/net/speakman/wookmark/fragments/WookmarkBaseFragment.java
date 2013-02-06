package nz.net.speakman.wookmark.fragments;

import android.content.Context;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class WookmarkBaseFragment extends SherlockFragment {
	
	private static WookmarkBaseFragment mCurrentWookmark = null;
	
	public abstract String getTitle(Context ctx);
	
	public static WookmarkBaseFragment getCurrentWookmark() {
		return mCurrentWookmark;
	}

	public static void setCurrentWookmark(WookmarkBaseFragment mCurrentWookmark) {
		WookmarkBaseFragment.mCurrentWookmark = mCurrentWookmark;
	}
}
