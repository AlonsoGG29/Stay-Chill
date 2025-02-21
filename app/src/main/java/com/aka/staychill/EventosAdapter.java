package com.aka.staychill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class EventosAdapter  extends RecyclerView.Adapter<EventosAdapter .EventoViewHolder> {
    private List<Evento> eventos;
    private Context context;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Evento evento);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }



    public EventosAdapter (Context context, List<Evento> eventos) {
        this.context = context;
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.eventos, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventos.get(position);
        holder.tvNombreEvento.setText(evento.getNombre());
        holder.tvTipoEvento.setText(evento.getTipoDeEvento());

        // Imagen del evento
        if(evento.getImagenDelEvento() != 0){
            holder.ivEvento.setImageResource(evento.getImagenDelEvento());
        } else {
            holder.ivEvento.setImageResource(R.drawable.event_viajes);
        }

        // Foto de perfil con caché dinámica
        String urlFotoPerfil = evento.getCreadorProfileImage();

        if (urlFotoPerfil != null && !urlFotoPerfil.isEmpty()) {
            // Agrega parámetro de tiempo para evitar caché
            String urlConCacheBuster = urlFotoPerfil + "?v=" + System.currentTimeMillis();

            Glide.with(context)
                    .load(urlConCacheBuster)
                    .placeholder(R.drawable.event_viajes)
                    .error(R.drawable.event_viajes) // Imagen por defecto si hay error
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Desactiva caché de disco
                    .skipMemoryCache(true) // Desactiva caché en memoria
                    .into(holder.ivPerfil);
        } else {
            holder.ivPerfil.setImageResource(R.drawable.event_viajes);
        }

    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public void setEventos(List<Evento> nuevosEventos) {
        this.eventos = nuevosEventos;
        notifyDataSetChanged();
    }

    public class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreEvento, tvTipoEvento;
        ImageView ivEvento, ivPerfil;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreEvento = itemView.findViewById(R.id.nombreUsr);
            tvTipoEvento = itemView.findViewById(R.id.tipoDeEvento);
            ivEvento = itemView.findViewById(R.id.img_evento);
            ivPerfil = itemView.findViewById(R.id.imagen); // Suponiendo que este ImageView muestra la foto de perfil

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mListener != null) {
                    mListener.onItemClick(eventos.get(position));
                }
            });
        }
    }
}
