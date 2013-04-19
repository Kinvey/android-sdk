/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.samples.citywatch;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;


public class CityWatchListFragment extends SherlockFragment implements
		OnItemClickListener {

	private static final String TAG = CityWatchListFragment.class
			.getSimpleName();

	private ListView mList;
	private CityWatchAdapter mAdapter;

	public static CityWatchListFragment newInstance() {
		return new CityWatchListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_list, group, false);
		bindViews(v);
		return v;
	}

	private void bindViews(View v) {

		List<com.kinvey.samples.citywatch.CityWatchEntity> ents = ((CityWatch) getSherlockActivity()).nearbyEntities;

		// ONLY do this if there are any entities, else TODO add a loading
		// indicator
		mList = (ListView) v.findViewById(R.id.list);
		mAdapter = new CityWatchAdapter(getSherlockActivity(), ents,
				(LayoutInflater) getSherlockActivity().getSystemService(
						Activity.LAYOUT_INFLATER_SERVICE));

		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		Intent details = new Intent(getSherlockActivity(),
				CityWatchDetailsActivity.class);
		details.putExtra(CityWatchDetailsActivity.EXTRA_FRAG_TARGET,
				CityWatchDetailsActivity.FRAG_VIEW);

		details.putExtra(CityWatchDetailsActivity.EXTRA_ENTITY, mAdapter.getItem(position));


		startActivity(details);

	}

	public void notifyNewData(List<com.kinvey.samples.citywatch.CityWatchEntity> n) {
		Log.i(TAG, "notifying new data");
		mAdapter.clear();
		mAdapter.addAll(n);
		mAdapter.notifyDataSetChanged();

	}

	/**
	 *
	 * This Adapter is used to maintain data and push individual row views to
	 * the ListView object, note it constructs the Views used by each row and
	 * uses the ViewHolder pattern.
	 *
	 */
	private class CityWatchAdapter extends ArrayAdapter<com.kinvey.samples.citywatch.CityWatchEntity> {

		private LayoutInflater mInflater;

		public CityWatchAdapter(Context context, List<com.kinvey.samples.citywatch.CityWatchEntity> objects,
				LayoutInflater inf) {
			// NOTE: I pass an arbitrary textViewResourceID to the super
			// constructor-- Below I override
			// getView(...), which causes the underlying adapter to ignore this
			// field anyways, it is just needed in the constructor.
			super(context, R.id.list_comment, objects);
			this.mInflater = inf;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			ImageView pic = null;
			TextView name = null;
			TextView address = null;
			TextView comment = null;

			TextView distance = null;
			TextView time = null;

			com.kinvey.samples.citywatch.CityWatchEntity rowData = getItem(position);

			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.list_row, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();

			pic = holder.getPic();

			if (rowData.getBitmap() != null) {
				pic.setImageBitmap(rowData.getBitmap());
			}else{
				pic.setImageResource(R.drawable.ic_menu_camera);
			}

			name = holder.getName();
			name.setText(rowData.getTitle());

			address = holder.getAddress();
			address.setText(rowData.getAddress());

			comment = holder.getComment();
			comment.setText(rowData.getDescription());

			// rowData.get

			// pic = holder.getPic();

			// name.setText(rowData.get());

			// address = holder.getAddress();
			// address.setText(rowData.getAddress());

			// category = holder.getCategory();
			// category.setText(rowData.getCategory());

			// comment = holder.getComment();
			// comment.setText(rowData.getDescription());
			// distance = holder.getDistance();
			// distance.setText(rowData.getCoords().toString());
			//
			// time = holder.getTime();
			// time.setText(rowData.get)

			return convertView;
		}

		/**
		 * This pattern is used as an optimization for Android ListViews.
		 *
		 * Since every row uses the same layout, the View object itself can be
		 * recycled, only the data/content of the row has to be updated.
		 *
		 * This allows for Android to only inflate enough Row Views to fit on
		 * screen, and then they are recycled. This allows us to avoid creating
		 * a new view for every single row, which can have a bad effect on
		 * performance (especially with large lists on large screen devices).
		 *
		 */
		private class ViewHolder {
			private View mRow;
			private ImageView pic = null;
			private TextView address = null;
			private TextView name = null;
			private TextView comment = null;
			private TextView distance = null;
			private TextView time = null;

			public ViewHolder(View row) {
				mRow = row;
			}

			public ImageView getPic() {
				if (null == pic) {
					pic = (ImageView) mRow.findViewById(R.id.list_image);
				}
				return pic;
			}

			public TextView getAddress() {
				if (null == address) {
					address = (TextView) mRow.findViewById(R.id.list_address);
				}
				return address;
			}

			public TextView getName() {
				if (null == name) {
					name = (TextView) mRow.findViewById(R.id.list_name);
				}
				return name;
			}

			public TextView getComment() {
				if (null == comment) {
					comment = (TextView) mRow.findViewById(R.id.list_comment);
				}
				return comment;
			}

			public TextView getDistance() {
				if (null == distance) {
					distance = (TextView) mRow.findViewById(R.id.list_distance);
				}
				return distance;
			}

			public TextView getTime() {
				if (null == time) {
					time = (TextView) mRow.findViewById(R.id.list_time);
				}
				return time;
			}

		}
	}

}
