package net.osmand.plus.sherpafy;

import java.util.List;

import net.osmand.access.AccessibleAlertBuilder;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.GPXUtilities.WptPt;
import net.osmand.plus.GpxSelectionHelper.SelectedGpxFile;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.sherpafy.TourInformation.StageInformation;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SherpafyStageFragment extends SherlockFragment {
	public static final String STAGE_PARAM = "STAGE";
	public static final String TOUR_PARAM = "TOUR";
	private static final int START = 8;
	OsmandApplication app;
	private SherpafyCustomization customization;
	private StageInformation stage;
	private TourInformation tour;
	private View view;

	public SherpafyStageFragment() {
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (OsmandApplication) getSherlockActivity().getApplication();
		customization = (SherpafyCustomization) app.getAppCustomization();

		setHasOptionsMenu(true);
		String id = getArguments().getString(TOUR_PARAM);
		for(TourInformation ti : customization.getTourInformations()) {
			if(ti.getId().equals(id)) {
				tour = ti;
				getSherlockActivity().getSupportActionBar().setTitle(tour.getName());
				break;
			}
		}
		int k = getArguments().getInt(STAGE_PARAM);
		if(tour != null && tour.getStageInformation().size() > k) {
			stage = tour.getStageInformation().get(k);
			getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.tab_stage) + " " + (k+1));
		}
	}
	



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// createMenuItem(menu, ACTION_GO_TO_MAP, R.string.start_tour, 0, 0,/* R.drawable.ic_action_marker_light, */
		// MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		if (tour != null) {
			((TourViewActivity) getSherlockActivity()).createMenuItem(menu, START, R.string.start_tour,
					0, 0,
					MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == START) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.sherpafy_stage_info, container, false);
		WebView description = (WebView) view.findViewById(R.id.Description);
		ImageView icon = (ImageView) view.findViewById(R.id.Icon);
		TextView additional = (TextView) view.findViewById(R.id.AdditionalText);
		TextView text = (TextView) view.findViewById(R.id.Text);
		TextView header = (TextView) view.findViewById(R.id.HeaderText);

		if(stage.getImageBitmap() != null) {
			icon.setImageBitmap(stage.getImageBitmap()) ;
		} else {
			icon.setVisibility(View.GONE);
		}
		if (stage.getDistance() > 0) {
			additional.setText(OsmAndFormatter.getFormattedDistance((float) stage.getDistance(), getMyApplication()));
		} else {
			additional.setText("");
		}
		header.setText(stage.getName());
		text.setText(stage.getShortDescription());
		description.loadData("<html><body>" + stage.getFullDescription() + "</body></html", "text/html", "utf-8");
		return view;
	}
	
	

	private ImageGetter getImageGetter(final View v) {
		return new Html.ImageGetter() {
			@Override
			public Drawable getDrawable(String s) {
				Bitmap file = customization.getSelectedTour().getImageBitmapFromPath(s);
				v.setTag(file);
				Drawable bmp = new BitmapDrawable(getResources(), file);
				// if image is thicker than screen - it may cause some problems, so we need to scale it
				int imagewidth = bmp.getIntrinsicWidth();
				// TODO
//				if (displaySize.x - 1 > imagewidth) {
//					bmp.setBounds(0, 0, bmp.getIntrinsicWidth(), bmp.getIntrinsicHeight());
//				} else {
//					double scale = (double) (displaySize.x - 1) / imagewidth;
//					bmp.setBounds(0, 0, (int) (scale * bmp.getIntrinsicWidth()),
//							(int) (scale * bmp.getIntrinsicHeight()));
//				}
				return bmp;
			}

		};
	}
	


	private void addOnClickListener(final TextView tv) {
		tv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof Bitmap) {
					final AccessibleAlertBuilder dlg = new AccessibleAlertBuilder(getActivity());
					dlg.setPositiveButton(R.string.default_buttons_ok, null);
					ScrollView sv = new ScrollView(getActivity());
					ImageView img = new ImageView(getActivity());
					img.setImageBitmap((Bitmap) tv.getTag());
					sv.addView(img);
					dlg.setView(sv);
					dlg.show();
				}
			}
		});
	}

	private void prepareBitmap(Bitmap imageBitmap) {
		ImageView img = null;
		if (imageBitmap != null) {
			img.setImageBitmap(imageBitmap);
			img.setAdjustViewBounds(true);
			img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			img.setCropToPadding(true);
			img.setVisibility(View.VISIBLE);
		} else {
			img.setVisibility(View.GONE);
		}
	}

	private void goToMap() {
		if (customization.getSelectedStage() != null) {
			GPXFile gpx = customization.getSelectedStage().getGpx();
			List<SelectedGpxFile> sgpx = getMyApplication().getSelectedGpxHelper().getSelectedGPXFiles();
			if (gpx == null && sgpx.size() > 0) {
				getMyApplication().getSelectedGpxHelper().clearAllGpxFileToShow();
			} else if (sgpx.size() != 1 || sgpx.get(0).getGpxFile() != gpx) {
				getMyApplication().getSelectedGpxHelper().clearAllGpxFileToShow();
				if (gpx != null && gpx.findPointToShow() != null) {
					WptPt p = gpx.findPointToShow();
					getMyApplication().getSettings().setMapLocationToShow(p.lat, p.lon, 16, null);
					getMyApplication().getSelectedGpxHelper().setGpxFileToDisplay(gpx);
				}
			}
		}
		Intent newIntent = new Intent(getActivity(), customization.getMapActivity());
		newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		this.startActivityForResult(newIntent, 0);
	}
	
	private OsmandApplication getMyApplication() {
		return (OsmandApplication) getActivity().getApplication();
	}
	
}