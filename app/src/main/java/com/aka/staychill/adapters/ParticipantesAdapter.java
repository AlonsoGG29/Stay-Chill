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

    public ParticipantesAdapter(Context ctx, List<Usuario> list) {
        this.context = ctx;
        this.participantes = list;
    }

    public void setParticipantes(List<Usuario> list) {
        this.participantes = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_perfil, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Usuario u = participantes.get(pos);
        h.nombre.setText(u.getNombre() + " " + u.getApellido());
        String url = u.getImagenPerfil();
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.img_default)
                    .into(h.imagen);
        } else {
            h.imagen.setImageResource(R.drawable.img_default);
        }
    }

    @Override public int getItemCount() {
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
