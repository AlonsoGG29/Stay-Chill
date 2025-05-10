package com.aka.staychill.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aka.staychill.R;
import com.aka.staychill.types.Usuario;
import com.bumptech.glide.Glide;

import java.util.List;

public class ParticipantesAdapter extends RecyclerView.Adapter<ParticipantesAdapter.ViewHolder> {
    private final Context context;
    private List<Usuario> participantes;
    private OnItemClickListener listener;

    // Interfaz para manejar los clicks en cada participante
    public interface OnItemClickListener {
        void onItemClick(Usuario usuario);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ParticipantesAdapter(Context ctx, List<Usuario> list) {
        this.context = ctx;
        this.participantes = list;
    }

    public void setParticipantes(List<Usuario> list) {
        this.participantes = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_perfil, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Usuario u = participantes.get(pos);
        holder.nombre.setText(u.getNombre() + " " + u.getApellido());
        String url = u.getImagenPerfil();
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.img_default)
                    .into(holder.imagen);
        } else {
            holder.imagen.setImageResource(R.drawable.img_default);
        }
        // Asignar click listener para que devuelva el objeto Usuario
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(u);
            }
        });
    }

    @Override
    public int getItemCount() {
        return participantes == null ? 0 : participantes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;
        ViewHolder(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagenPerfil);
            nombre = itemView.findViewById(R.id.nombreUsr);
        }
    }
}