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
import com.aka.staychill.CargarImagenes;
import com.aka.staychill.types.Notificacion;
import java.util.List;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

    private final Context context;
    private List<Notificacion> notificaciones;
    private final CargarImagenes cargadorImagenes;

    // ViewHolder interno
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMensaje, tvHora, tvNombreUsuario;
        public ImageView ivPerfil;

        public ViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.nombreUsr);
            tvHora = itemView.findViewById(R.id.horaNotificacion);
            ivPerfil = itemView.findViewById(R.id.imagenPerfil);
        }
    }

    // Constructor
    public NotificacionesAdapter(Context context, List<Notificacion> notificaciones) {
        this.context = context;
        this.notificaciones = notificaciones;
        this.cargadorImagenes = CargarImagenes.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion notificacion = notificaciones.get(position);

        // 1. Mensaje principal
        String mensaje = notificacion.getMensaje() != null
                ? notificacion.getMensaje()
                : "Nueva notificación";

        // 2. Hora formateada
        holder.tvHora.setText(notificacion.getFechaFormateada());

        // 3. Datos del usuario emisor (si existe)
        if (notificacion.getUsuarioEmisor() != null) {
            Notificacion.Usuario usuario = notificacion.getUsuarioEmisor();


            // Construir mensaje con nombre
            String mensajeCompleto = usuario.getNombre() + ": " + mensaje;
            holder.tvMensaje.setText(mensajeCompleto);

            // Cargar imagen de perfil
            cargadorImagenes.loadProfileImage(
                    usuario.getFotoUrl(),
                    holder.ivPerfil,
                    context
            );

        } else { // Caso de datos incompletos
            holder.tvMensaje.setText(mensaje);
            cargadorImagenes.loadProfileImage("", holder.ivPerfil, context);
        }

        // 4. Opcional: Nombre separado (si el layout lo incluye)
        if (holder.tvNombreUsuario != null && notificacion.getUsuarioEmisor() != null) {
            holder.tvNombreUsuario.setText(notificacion.getUsuarioEmisor().getNombre());
        }
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    // Método para actualizar datos
    public void actualizarNotificaciones(List<Notificacion> nuevasNotificaciones) {
        this.notificaciones = nuevasNotificaciones;
        notifyDataSetChanged();
    }
}