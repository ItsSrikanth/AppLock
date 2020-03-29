package dev.srikanth.applock.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.srikanth.applock.R;
import dev.srikanth.applock.pojos.AppInfo;
import dev.srikanth.applock.preferences.SettingsPreferences;

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {
    private List<AppInfo> appsList;
    private static final String TAG = "RAdapter";

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textView;
        private ImageView img,lock;


        //This is the subclass ViewHolder which simply
        //'holds the views' for us to show on each row
        public ViewHolder(View itemView) {
            super(itemView);
            //Finds the views from our row.xml
            textView = (TextView) itemView.findViewById(R.id.text);
            img = (ImageView) itemView.findViewById(R.id.img);
            lock = itemView.findViewById(R.id.lock);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick (View v) {
            int pos = getAdapterPosition();
            boolean lock = appsList.get(pos).isLock();
            appsList.get(pos).setLock(!lock);
            notifyDataSetChanged();
            SettingsPreferences preferences = SettingsPreferences.getInstance();
            String lockedApps = preferences.getLockedApps();
            if (appsList.get(pos).isLock()){
                lockedApps = lockedApps+String.valueOf(appsList.get(pos).getPackageName())+"|";
            }else {
                lockedApps = lockedApps.replace(String.valueOf(appsList.get(pos).getPackageName())+"|", "");
            }
            Log.e(TAG, "onClick: "+lockedApps );
            preferences.setLockedApps(lockedApps);
            /*Context context = v.getContext();
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(String.valueOf(appsList.get(pos).getPackageName()));
            context.startActivity(launchIntent);*/
//            Toast.makeText(v.getContext(), appsList.get(pos).getLabel(), Toast.LENGTH_LONG).show();
        }
    }



    public RAdapter(Context c, List<AppInfo> allowedApps) {

        appsList = new ArrayList<AppInfo>();
        appsList.clear();
        appsList.addAll(allowedApps);
    }

    @Override
    public void onBindViewHolder(RAdapter.ViewHolder viewHolder, int i) {

        //Here we use the information in the list we created to define the views

        String appLabel = String.valueOf(appsList.get(i).getLabel());
        String appPackage = String.valueOf(appsList.get(i).getPackageName());
        Drawable appIcon = appsList.get(i).getIcon();
        boolean lock = appsList.get(i).isLock();

        TextView textView = viewHolder.textView;
        textView.setText(appLabel);
        ImageView imageView = viewHolder.img;
        imageView.setImageDrawable(appIcon);
        ImageView imageLock = viewHolder.lock;
        if (lock) imageLock.setImageResource(R.drawable.ic_lock_outline_black_24dp);
        else imageLock.setImageResource(R.drawable.ic_lock_open_black_24dp);
    }


    @Override
    public int getItemCount() {

        //This method needs to be overridden so that Androids knows how many items
        //will be making it into the list

        return appsList.size();
    }


    @Override
    public RAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
}