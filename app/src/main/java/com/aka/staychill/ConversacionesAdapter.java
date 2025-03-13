package com.aka.staychill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ConversacionesAdapter extends RecyclerView.Adapter<ConversacionesAdapter.ViewHolder> {
    private List<Conversacion> conversaciones;
    private OnConversacionClickListener listener;
    private Context context;


    public List<Conversacion> getConversaciones() {
        return conversaciones;
    }

    public ConversacionesAdapter(List<Conversacion> conversaciones,
                                 OnConversacionClickListener listener,
                                 Context context) {
        this.conversaciones = conversaciones;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversaciones, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversacion conversacion = conversaciones.get(position);

        holder.tvNombre.setText(conversacion.getNombre());
        holder.tvMensaje.setText(conversacion.getUltimoMensaje());

        Glide.with(context)
                .load(conversacion.getFoto())
                .placeholder(R.drawable.img_default)
                .circleCrop()
                .into(holder.ivFoto);

        holder.itemView.setOnClickListener(v ->
                listener.onConversacionClick(conversacion.getId()));
    }


    public interface OnConversacionClickListener {
        void onConversacionClick(String usuarioId);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMensaje;
        ImageView ivFoto;

        ViewHolder(View view) {
            super(view);
            tvNombre = view.findViewById(R.id.nombreChat);
            tvMensaje = view.findViewById(R.id.mensajesChat);
            ivFoto = view.findViewById(R.id.fotoPerfilChat);
        }
    }
    public int getItemCount() {
        return conversaciones.size();
    }
    public void actualizarDatos(List<Conversacion> nuevasConversaciones) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return conversaciones.size(); }

            @Override
            public int getNewListSize() { return nuevasConversaciones.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return conversaciones.get(oldPos).getId().equals(
                        nuevasConversaciones.get(newPos).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return conversaciones.get(oldPos).equals(
                        nuevasConversaciones.get(newPos));
            }
        });

        conversaciones.clear();
        conversaciones.addAll(nuevasConversaciones);
        result.dispatchUpdatesTo(this);
    }


}