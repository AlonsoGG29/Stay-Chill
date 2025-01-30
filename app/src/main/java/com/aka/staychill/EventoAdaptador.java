package com.aka.staychill;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.circleCrop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;

public class EventoAdaptador extends RecyclerView.Adapter<EventoAdaptador.MyViewHolder> implements Filterable {

    private List<Evento> eventoList;
    private List<Evento> eventoListFull;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textViewSecondary;
        public MyViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.imagen);
            textView = v.findViewById(R.id.texto1);
            textViewSecondary = v.findViewById(R.id.texto2);
        }
    }

    public EventoAdaptador(List<Evento> items) {
        eventoList = items;
        eventoListFull = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.eventos, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Evento currentItem = eventoList.get(position);
        Glide.with(holder.imageView.getContext())
                .load(currentItem.getImageUrl())
                .circleCrop()
                .into(holder.imageView);

        holder.textView.setText(currentItem.getText());
        holder.textViewSecondary.setText(currentItem.getSecondaryText());
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    @Override
    public Filter getFilter() {
        return eventoFilter;
    }

    private final Filter eventoFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Evento> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(eventoListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Evento item : eventoListFull) {
                    if (item.getText().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            eventoList.clear();
            eventoList.addAll((List) results.values);

            // Use a better method to notify changes in specific range
            notifyDataSetChanged(); // Use notifyDataSetChanged as a last resort
        }
    };
}
