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

import java.util.List;

public class EventosAdapter  extends RecyclerView.Adapter<EventosAdapter .EventoViewHolder> {
    private List<Evento> eventos;
    private Context context;

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

        // Se asume que 'imagenDelEvento' ya es el ID del drawable
        if(evento.getImagenDelEvento() != 0){
            holder.ivEvento.setImageResource(evento.getImagenDelEvento());
        } else {
            holder.ivEvento.setImageResource(R.drawable.event_viajes);
        }

        // Para la foto de perfil del usuario, podrías usar Glide o Picasso:
        String urlFotoPerfil = SupabaseConfig.getSupabaseUrl()+"/storage/v1/object/public/user_files/user_uploads/"
                + evento.getCreadorId().toString() + "_avatar.jpg";
        Glide.with(context)
                .load(urlFotoPerfil)
                .placeholder(R.drawable.event_viajes)
                .circleCrop()// imagen por defecto mientras carga
                .into(holder.ivPerfil);
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
        }
    }
}
