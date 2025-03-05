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

public class ConversacionesAdapter extends RecyclerView.Adapter<ConversacionesAdapter.ViewHolder> {

    private List<Conversacion> conversaciones;
    private OnConversacionClickListener listener;
    private Context context; // Necesario para Glide

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

    // ... (onCreateViewHolder y interface OnConversacionClickListener)

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversacion conversacion = conversaciones.get(position);

        // 1. Nombre y último mensaje
        holder.tvNombre.setText(conversacion.getNombre());
        holder.tvMensaje.setText(conversacion.getUltimoMensaje());

        // 2. Cargar imagen de perfil con Glide
        String fotoUrl = conversacion.getFoto();
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(context)
                    .load(fotoUrl)
                    .placeholder(R.drawable.img_default) // Imagen mientras carga
                    .error(R.drawable.img_default) // Imagen si falla
                    .circleCrop() // Recortar como círculo
                    .into(holder.ivFoto);
        } else {
            // Si no hay URL, mostrar placeholder
            holder.ivFoto.setImageResource(R.drawable.img_default);
        }

        // 3. Click en la conversación
        holder.itemView.setOnClickListener(v -> {
            listener.onConversacionClick(conversacion.getContactoId());
        });
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
            ivFoto = view.findViewById(R.id.fotoPerfilChat); // Asegúrate de que este ID existe en tu XML
        }
    }
    public int getItemCount() {
        return conversaciones.size(); // Devuelve el número de conversaciones
    }

}