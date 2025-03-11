package com.aka.staychill;

import com.google.gson.annotations.SerializedName;

public class Mensaje {
    @SerializedName("id")
    private String id;

    @SerializedName("conversacion_id")
    private String conversacionId;

    @SerializedName("sender_id")
    private Sender sender; // Usamos una clase anidada para el remitente

    @SerializedName("contenido")
    private String contenido;

    @SerializedName("fecha")
    private String fecha;

    // Clase para el remitente
    public static class Sender {
        @SerializedName("foren_uid")
        private String id;

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("profile_image_url")
        private String foto;

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getFoto() { return foto; }
    }

    // Getters
    public String getId() { return id; }
    public Sender getSender() { return sender; }
    public String getContenido() { return contenido; }
    public String getFecha() { return fecha; }

    public String getConversacionId() {
        return conversacionId;
    }


}