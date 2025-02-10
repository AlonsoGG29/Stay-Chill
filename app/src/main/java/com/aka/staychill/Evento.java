package com.aka.staychill;

public class Evento {

    //Clase que se comunica con MiAdaptador y le pasa la información

    private final String imageUrl;
    private final String text;
    private final String imageUrl2;
    private final String nombre;
    private final String localizacion;
    private final String descripcion;
    private final String fecha;
    private final String hora;

    // Constructor para los nuevos campos
    public Evento(String nombre, String localizacion, String descripcion, String fecha, String hora) {
        this.nombre = nombre;
        this.localizacion = localizacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.imageUrl = null;
        this.text = null;
        this.imageUrl2 = null;
    }

    // Constructor para los antiguos campos
    public Evento(String imageUrl, String text, String imageUrl2) {
        this.imageUrl = imageUrl;
        this.text = text;
        this.imageUrl2 = imageUrl2;
        this.nombre = null;
        this.localizacion = null;
        this.descripcion = null;
        this.fecha = null;
        this.hora = null;
    }


    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }
}