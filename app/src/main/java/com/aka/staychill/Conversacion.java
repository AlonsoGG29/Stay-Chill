package com.aka.staychill;

import com.google.gson.annotations.SerializedName;

public class Conversacion {
    @SerializedName("id")
    private String id;


    @SerializedName("nombre")
    private String nombre;

    @SerializedName("participante1")
    private String participante1;

    @SerializedName("participante2")
    private String participante2;

    @SerializedName("profile_image_url")
    private String foto;

    @SerializedName("ultimo_mensaje")
    private String ultimoMensaje;

    @SerializedName("fecha")
    private String fecha;

    // Getters
    public String getId() {
        return id;
    }



    public String getNombre() {
        return nombre;
    }

    public String getFoto() {
        return foto;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public String getParticipante1() { return participante1; }



    public String getParticipante2() { return participante2; }



    // Setters
    public void setId(String id) {
        this.id = id;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    public void setParticipante1(String participante1) { this.participante1 = participante1; }

    public void setParticipante2(String participante2) { this.participante2 = participante2; }


}
