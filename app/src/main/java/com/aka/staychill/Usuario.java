package com.aka.staychill;

import com.google.gson.annotations.SerializedName;

public class Usuario {
    @SerializedName("foren_uid")
    private String uid;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("profile_image_url")
    private String imagenPerfil;

    @SerializedName("pais")
    private String pais;

    public Usuario(String uid, String nombre, String apellido, String imagenPerfil, String pais) {
        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.imagenPerfil = imagenPerfil;
        this.pais = pais;
    }

    // Getters
    public String getUid() { return uid; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getImagenPerfil() { return imagenPerfil; }
    public String getPais() { return pais; }
}