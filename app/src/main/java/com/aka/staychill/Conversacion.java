package com.aka.staychill;

import com.google.gson.annotations.SerializedName;

public class Conversacion {
    @SerializedName("contacto_id")
    private String contactoId;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("foto")
    private String foto;

    @SerializedName("ultimo_mensaje")
    private String ultimoMensaje;

    @SerializedName("fecha")
    private String fecha;

    // Getters y Setters
    public String getContactoId() { return contactoId; }
    public String getNombre() { return nombre; }
    public String getFoto() { return foto; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public String getFecha() { return fecha; }
}