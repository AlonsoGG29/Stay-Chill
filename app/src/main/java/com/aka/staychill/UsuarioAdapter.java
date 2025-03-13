package com.aka.staychill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {
    private List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;


    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    public interface OnUsuarioClickListener {
        void onUsuarioClick(String usuarioId);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoNombre, textoPais;
        ImageView imagenPerfil;

        public ViewHolder(View vista) {
            super(vista);
            textoNombre = vista.findViewById(R.id.nombreUsr);
            imagenPerfil = vista.findViewById(R.id.imagenPerfil);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup padre, int tipoVista) {
        View vista = LayoutInflater.from(padre.getContext())
                .inflate(R.layout.perfil, padre, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Usuario usuario = listaUsuarios.get(posicion);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUsuarioClick(usuario.getId());
            }
        });

        String nombreCompleto = (usuario.getNombre() != null ? usuario.getNombre() : "")
                + " "
                + (usuario.getApellido() != null ? usuario.getApellido() : "");
        holder.textoNombre.setText(nombreCompleto.trim());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUsuarioClick(usuario.getId());
            }
        });



        if (usuario.getImagenPerfil() != null && !usuario.getImagenPerfil().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(usuario.getImagenPerfil())
                    .placeholder(R.drawable.img_default)
                    .circleCrop()
                    .into(holder.imagenPerfil);
        } else {
            holder.imagenPerfil.setImageResource(R.drawable.img_default);
        }
    }
    public List<Usuario> getUsuarios() {
        return listaUsuarios;
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void actualizarLista(List<Usuario> nuevaLista) {
        listaUsuarios = nuevaLista;
        notifyDataSetChanged();
    }
}