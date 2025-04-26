package com.aka.staychill.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.aka.staychill.types.Evento;
import com.aka.staychill.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.EventoViewHolder> {

    private List<Evento> eventos;
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Evento evento);
    }

    public EventosAdapter(Context context, List<Evento> eventos) {
        this.context = context;
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_eventos, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventos.get(position);
        holder.bind(evento);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(evento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public void setEventos(List<Evento> nuevosEventos) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new EventoDiffCallback(this.eventos, nuevosEventos));
        this.eventos = nuevosEventos;
        result.dispatchUpdatesTo(this);
    }

    private static class EventoDiffCallback extends DiffUtil.Callback {
        private final List<Evento> oldList;
        private final List<Evento> newList;

        public EventoDiffCallback(List<Evento> oldList, List<Evento> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getId().equals(newList.get(newPos).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).equals(newList.get(newPos));
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Clase ViewHolder optimizada
    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivEvento, ivPerfil;
        private final TextView tvNombre, tvTipo;

        EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializar todas las vistas de una vez
            ivEvento = itemView.findViewById(R.id.img_evento);
            ivPerfil = itemView.findViewById(R.id.imgPerfil);
            tvNombre = itemView.findViewById(R.id.nombreUsr);
            tvTipo = itemView.findViewById(R.id.tipoDeEvento);
        }

        void bind(Evento evento) {
            tvNombre.setText(evento.getNombreEvento());
            tvTipo.setText(evento.getTipoDeEvento());
            cargarImagenes(evento);
        }

        private void cargarImagenes(Evento evento) {
            // Obtener contexto una sola vez
            Context context = itemView.getContext();


            // 1. Imagen del evento
            int imagenResId = evento.getImagenDelEvento(context);
            int colorRes = evento.getColorResId();
            if (imagenResId != 0) {
                Glide.with(context)
                        .load(imagenResId)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_default) // Muestra mientras carga
                        .error(R.drawable.img_default) // Muestra si falla
                        .into(ivEvento);
            }
            ivEvento.setBackgroundColor(ContextCompat.getColor(context, colorRes));

            // 2. Foto de perfil
            String urlPerfil = evento.getCreadorProfileImage();
            if (urlPerfil != null && !urlPerfil.isEmpty()) {
                Glide.with(context)
                        .load(urlPerfil)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.img_default) // Placeholder para perfil
                        .into(ivPerfil);
            }

        }
    }
}