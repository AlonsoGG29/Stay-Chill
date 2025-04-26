package com.aka.staychill.types;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Notificacion {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userIdDestino;

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("relacion_id")
    private String relacionId;

    @SerializedName("leido")
    private boolean leido;

    @SerializedName("fecha_creacion")
    private Date fechaCreacion;

    @SerializedName("usuarios")
    private Usuario usuarioEmisor;

    // Getters
    public String getId() { return id; }
    public String getUserIdDestino() { return userIdDestino; }
    public String getMensaje() { return mensaje; }
    public String getTipo() { return tipo; }
    public String getRelacionId() { return relacionId; }
    public boolean isLeido() { return leido; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public Usuario getUsuarioEmisor() { return usuarioEmisor; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserIdDestino(String userIdDestino) { this.userIdDestino = userIdDestino; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setRelacionId(String relacionId) { this.relacionId = relacionId; }
    public void setLeido(boolean leido) { this.leido = leido; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setUsuarioEmisor(Usuario usuarioEmisor) { this.usuarioEmisor = usuarioEmisor; }

    // Clase interna para el usuario (emisor de la notificación)
    public static class Usuario {
        @SerializedName("nombre")
        private String nombre;

        @SerializedName("profile_image_url")
        private String fotoUrl;

        public String getNombre() {
            return nombre != null ? nombre : "Usuario";
        }
        public String getFotoUrl() {
            return fotoUrl != null ? fotoUrl : "";
        }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    }

    // Método para formatear la hora
    public String getHoraFormateada() {
        if (fechaCreacion == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(fechaCreacion);
    }
}
