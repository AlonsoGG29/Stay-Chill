package com.aka.staychill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;
import java.util.ArrayList;
import android.widget.Filter;
import android.widget.Filterable;

public class EventoAdaptador extends RecyclerView.Adapter<EventoAdaptador.MiViewHolder> implements Filterable {

    private final List<Evento> eventoList;
    private final List<Evento> eventoListFull;

    public static class MiViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public ImageView imageView2;
        public MiViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.imagen);
            textView = v.findViewById(R.id.texto1);
            imageView2 = v.findViewById(R.id.img_evento);
        }
    }

    public EventoAdaptador(List<Evento> items) {
        this.eventoList = items;
        this.eventoListFull = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public MiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.eventos, parent, false);
        return new MiViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MiViewHolder holder, int position) {
        Evento currentItem = eventoList.get(position);
        Glide.with(holder.imageView.getContext())
                .load(currentItem.getImageUrl())
                .circleCrop()
                .into(holder.imageView);

        holder.textView.setText(currentItem.getText());
        Glide.with(holder.imageView2.getContext())
                .load(currentItem.getImageUrl2())
                .fitCenter()
                .into(holder.imageView2);
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    @Override
    public Filter getFilter() {
        return filtroEvento;
    }

    private final Filter filtroEvento = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Evento> listaFiltrada = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                listaFiltrada.addAll(eventoListFull);
            } else {
                String patronFiltro = constraint.toString().toLowerCase().trim();

                for (Evento item : eventoListFull) {
                    if (item.getText().toLowerCase().contains(patronFiltro)) {
                        listaFiltrada.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = listaFiltrada;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            List<Evento> filteredList = (List<Evento>) results.values;

            eventoList.clear();
            eventoList.addAll(filteredList);

            // Utiliza métodos más específicos cuando sea posible
            notifyItemRangeChanged(0, eventoList.size());
        }
    };
}
