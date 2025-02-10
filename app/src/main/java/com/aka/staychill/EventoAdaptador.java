package com.aka.staychill;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class EventoAdaptador extends RecyclerView.Adapter<EventoAdaptador.EventoViewHolder> implements Filterable {

    private final List<Evento> eventoList;
    private final List<Evento> eventoListFull;

    public EventoAdaptador(List<Evento> eventoList) {
        this.eventoList = eventoList;
        this.eventoListFull = new ArrayList<>(eventoList);
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.eventos, parent, false);
        return new EventoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventoList.get(position);


        if (evento.getText() != null) {
            holder.textView.setText(evento.getNombre());
        }

        holder.nombreView.setText(evento.getNombre());
        holder.localizacionView.setText(evento.getLocalizacion());
        holder.descripcionView.setText(evento.getDescripcion());
        holder.fechaView.setText(evento.getFecha());
        holder.horaView.setText(evento.getHora());

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, EventoClick.class);
            intent.putExtra("nombreEvento", evento.getNombre());
            intent.putExtra("ubicacionEvento", evento.getLocalizacion());
            intent.putExtra("descripcionEvento", evento.getDescripcion());
            intent.putExtra("fechaEvento", evento.getFecha());
            intent.putExtra("horaEvento", evento.getHora());
            context.startActivity(intent);
        });
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
                    if (item.getNombre().toLowerCase().contains(filterPattern)) {
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
            notifyDataSetChanged();
        }
    };

    static class EventoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        ImageView imageView2;
        TextView nombreView;
        TextView localizacionView;
        TextView descripcionView;
        TextView fechaView;
        TextView horaView;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagen);
            textView = itemView.findViewById(R.id.texto1);
            imageView2 = itemView.findViewById(R.id.img_evento);
            nombreView = itemView.findViewById(R.id.tituloEvento);
            localizacionView = itemView.findViewById(R.id.ubicacionEvento);
            descripcionView = itemView.findViewById(R.id.descripcionEvento);
            fechaView = itemView.findViewById(R.id.fechaEvento);
            horaView = itemView.findViewById(R.id.horaEvento);
        }
    }
}