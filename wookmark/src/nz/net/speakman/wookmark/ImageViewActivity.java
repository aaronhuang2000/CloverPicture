package nz.net.speakman.wookmark;

import nz.net.speakman.wookmark.fragments.WookmarkBaseFragment;
import nz.net.speakman.wookmark.fragments.imageviewfragments.WookmarkBaseImageViewFragment;
import nz.net.speakman.wookmark.images.WookmarkImage;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AnimationUtils;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.fedorvlasov.lazylist.ImageLoader;

public class ImageViewActivity extends SherlockActivity implements OnGestureListener {

	public static final int IMAGE_COUNT = 6;
	public static final String IMAGE_KEY = "ImageKey";
	
	private static ImageLoader[] mImageLoader = null;
	private static ImageView[] mImageViewer = null;
	private static int mPosition = -1;
	private static int mCurImage = -1;
	
	private WookmarkImage mImage;
	
	private ViewFlipper flipper;
	private GestureDetector detector;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Request Feature must be called before adding content.
		// Note this turns it on by default, ABS thing.
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.image_view);
		
		detector = new GestureDetector(this);
		flipper = (ViewFlipper) this.findViewById(R.id.ViewFlipper1);
		
	//	if(mImageLoader == null) {
			mImageLoader = new ImageLoader[IMAGE_COUNT];
			mImageViewer = new ImageView[IMAGE_COUNT];
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
	    	Display display = wm.getDefaultDisplay();
	    	int scaleSize = Math.max(display.getWidth(), display.getHeight()) / 4;
			for (int i = 0; i < IMAGE_COUNT; i++) {
				mImageLoader[i] = new ImageLoader(getApplicationContext(), scaleSize);
				mImageViewer[i] = new ImageView(this);
				flipper.addView(mImageViewer[i]);
			}
	//	}
		
		Intent intent = getIntent();
		mPosition = (int)intent.getIntExtra(IMAGE_KEY, -1);
		if (mPosition == -1)
			return;
		mCurImage = 1;
		//mImage = (WookmarkImage)intent.getParcelableExtra(IMAGE_KEY);

		prepareImages(0, IMAGE_COUNT / 2);	
		
		setTitle(mImage.getTitle());
	}

	private void prepareImages(int viewPos, int count) {
		for (int i = 0; i < count; i++) {
			mImage = (WookmarkImage)((WookmarkBaseImageViewFragment)WookmarkBaseFragment.getCurrentWookmark()).getItem(mPosition);
			if (mImage != null && viewPos < IMAGE_COUNT) {
				mImageLoader[viewPos].DisplayImage(mImage.getImageUri().toString(), mImageViewer[viewPos], false, new ImageLoader.OnImageLoadFinishedListener() {
					@Override
					public void onFinished() {
						setSupportProgressBarIndeterminateVisibility(false);
					}
				});
				viewPos++;
			}
			mPosition++;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.image_view_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.image_view_menu_detail:
			showAdditionalDetailDialog();
			break;
		case R.id.image_view_menu_share:
			startShareIntent();
			break;
		case R.id.image_view_menu_wookmark_com:
			startWookmarkWebsiteIntent();
			break;
			// TODO Add a 'view on original site' link?
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void startShareIntent() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.image_view_share_content), mImage.getTitle(), mImage.getUrl()));
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.image_view_share_subject));
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.image_view_share_title)));
	}
	
	private void startWookmarkWebsiteIntent() {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW, mImage.getUrl());
		startActivity(sendIntent);
	}

	private void showAdditionalDetailDialog() {
		String[] details = new String[] { 
				String.format(getString(R.string.image_view_detail_image_title), mImage.getTitle()),
				String.format(getString(R.string.image_view_detail_image_width), mImage.getWidth()),
				String.format(getString(R.string.image_view_detail_image_height), mImage.getHeight()),
				String.format(getString(R.string.image_view_detail_image_referer), mImage.getRefererUri()) // TODO Auto-link this - make it clickable?
		};
		
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setTitle(R.string.image_view_detail_dialog_title)
		       .setItems(details, null)
		       .setNeutralButton(android.R.string.ok, null);

		// 3. Get the AlertDialog from create()
		builder.create().show();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return this.detector.onTouchEvent(event);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > 120) {
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_out));
			
			this.flipper.showNext();
			++mCurImage;
			if (mCurImage == IMAGE_COUNT / 2) {
				prepareImages(mCurImage, IMAGE_COUNT / 2);
			} else if (mCurImage == IMAGE_COUNT) {
				prepareImages(0, IMAGE_COUNT / 2);
			} else if (mCurImage > IMAGE_COUNT) {
				mCurImage = mCurImage % IMAGE_COUNT;
			}
			return true;
		} else if (e1.getX() - e2.getX() < -120) {
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_out));
			this.flipper.showPrevious();
			--mCurImage;
			if (mCurImage == IMAGE_COUNT / 2) {
				mPosition = Math.max(mPosition - IMAGE_COUNT, 0);
				prepareImages(0, IMAGE_COUNT / 2);
			} else if (mCurImage == 0) {
				mPosition = Math.max(mPosition - IMAGE_COUNT, 0);
				prepareImages(IMAGE_COUNT / 2, IMAGE_COUNT / 2);
			} else if (mCurImage < 0) {
				mCurImage = IMAGE_COUNT - Math.abs(mCurImage) % IMAGE_COUNT;
			}
			
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}
