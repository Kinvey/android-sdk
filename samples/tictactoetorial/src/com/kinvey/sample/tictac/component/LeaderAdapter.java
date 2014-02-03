package com.kinvey.sample.tictac.component;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinvey.sample.tictac.GameEntity;
import com.kinvey.sample.tictac.R;


public class LeaderAdapter extends ArrayAdapter<GameEntity> {

    private LayoutInflater mInflater;

    public LeaderAdapter(Context context, List<GameEntity> objects,
                            LayoutInflater inf) {
        // NOTE: I pass an arbitrary textViewResourceID to the super
        // constructor-- Below I override
        // getView(...), which causes the underlying adapter to ignore this
        // field anyways, it is just needed in the constructor.
        super(context, R.id.row_wins, objects);
        this.mInflater = inf;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeatureViewHolder holder = null;

        TextView name = null;
        TextView wins = null;
        TextView loses = null;
        ImageView gravatar = null;

        GameEntity rowData = getItem(position);

        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.row_leader, null);
            holder = new FeatureViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder = (FeatureViewHolder) convertView.getTag();

        name = holder.getName();
        name.setText(rowData.getPlayerName());
        wins = holder.getWins();
        wins.setText(rowData.getTotalWins() + " Wins");
        loses = holder.getLoses();
        loses.setText(rowData.getTotalLoses() + " Loses");
        if (rowData.getGravatar() != null){
            gravatar = holder.getGravatar();
            gravatar.setImageBitmap(rowData.getGravatar());
        }

        

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
     * a new view for every single row, which can have a negative effect on
     * performance (especially with large lists on large screen devices).
     *
     */
    private class FeatureViewHolder {
        private View row;

        private TextView wins = null;
        private TextView loses = null;
        private ImageView gravatar = null;
        private TextView name = null;

        public FeatureViewHolder(View row) {
            this.row = row;
        }

        public TextView getWins() {
            if (null == wins) {
                wins = (TextView) row.findViewById(R.id.row_wins);
            }
            return wins;
        }

        public TextView getLoses() {
            if (null == loses) {
                loses = (TextView) row.findViewById(R.id.row_loses);
            }
            return loses;
        }
        
        public ImageView getGravatar() {
            if (null == gravatar) {
                gravatar = (ImageView) row.findViewById(R.id.row_gravatar);
            }
            return gravatar;
        }
        
        public TextView getName() {
            if (null == name) {
            	name = (TextView) row.findViewById(R.id.row_name);
            }
            return name;
        }



    }
    
    
}
