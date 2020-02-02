package com.example.thesisitfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class LocationRecyclerViewAdapter extends
        RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder>
{
    private List<SingleRecyclerViewLocation> locationList;
    private MapboxMap mbMap;
    private WeakReference<map> weakReference;

    public LocationRecyclerViewAdapter(map activity, List<SingleRecyclerViewLocation> locationList, MapboxMap mapBoxMap)
    {
        this.locationList = locationList;
        this.mbMap = mapBoxMap;
        this.weakReference = new WeakReference<>(activity);
    }

    @Override
    public long getItemId(int position)
    {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_symbol_layer, parent, false);
        return new MyViewHolder(itemView);
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
        holder.name.setText(singleRecyclerViewLocation.getName());
        holder.setIsRecyclable(false);
        holder.setClickListener(new ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                //weakReference.get().directionsRouteList.
                weakReference.get()
                        .drawPolyline(weakReference.get().directionsRouteList.get(position));
                Toast.makeText(getApplicationContext(), "" + weakReference.get().directionsRouteList.get(0), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView name;
        CardView singleCard;
        ItemClickListener clickListener;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.evacName);
            singleCard = itemView.findViewById(R.id.single_location_cardview);
            singleCard.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener)
        {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v)
        {
            clickListener.onItemClick(v, getLayoutPosition());
        }
    }
}
