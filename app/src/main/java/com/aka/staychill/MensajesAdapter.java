package com.aka.staychill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MensajesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TIPO_ENVIADO = 1;
    private static final int TIPO_RECIBIDO = 2;

    private List<Mensaje> mensajes;
    private String usuarioActualId;

    public MensajesAdapter(List<Mensaje> mensajes, String usuarioActualId) {
        this.mensajes = mensajes;
        this.usuarioActualId = usuarioActualId;
    }

    @Override
    public int getItemViewType(int position) {
        return mensajes.get(position).getSenderId().equals(usuarioActualId)
                ? TIPO_ENVIADO : TIPO_RECIBIDO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TIPO_ENVIADO
                ? R.layout.mensaje_enviado
                : R.layout.mensaje_recibido;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);
        MensajeViewHolder vh = (MensajeViewHolder) holder;

        vh.tvMensaje.setText(mensaje.getContenido());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        try {
            Date date = inputFormat.parse(mensaje.getFecha());
            vh.tvHora.setText(outputFormat.format(date));
        } catch (ParseException e) {
            vh.tvHora.setText("");
        } // Formato HH:mm
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje, tvHora;

        MensajeViewHolder(View view) {
            super(view);
            tvMensaje = view.findViewById(R.id.tvMensaje);
            tvHora = view.findViewById(R.id.tvHora);
        }
    }
    public void setMensajes(List<Mensaje> nuevosMensajes) {
        this.mensajes.clear();
        this.mensajes.addAll(nuevosMensajes);
        notifyDataSetChanged(); // ¡Notifica los cambios!
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

}
