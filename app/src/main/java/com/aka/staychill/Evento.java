package com.aka.staychill;

import java.util.Date;
import java.util.UUID;

public class Evento {
    private String nombre;
    private String localizacion;
    private String descripcion;
    private Date fecha;
    private Date hora;
    private String tipoDeEvento;
    private int imagenDelEvento;
    private UUID creadorId;
    private String creadorProfileImage;  // Nuevo campo

    public Evento(String nombre, String localizacion, String descripcion, Date fecha, Date hora,
                  String tipoDeEvento, int imagenDelEvento, UUID creadorId, String creadorProfileImage) {
        this.nombre = nombre;
        this.localizacion = localizacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoDeEvento = tipoDeEvento;
        this.imagenDelEvento = imagenDelEvento;
        this.creadorId = creadorId;
        this.creadorProfileImage = creadorProfileImage; // Inicializar nuevo campo
    }


    // Getters
    public String getNombre() { return nombre; }
    public String getLocalizacion() { return localizacion; }
    public String getDescripcion() { return descripcion; }
    public Date getFecha() { return fecha; }
    public Date getHora() { return hora; }
    public String getTipoDeEvento() { return tipoDeEvento; }
    public int getImagenDelEvento() { return imagenDelEvento; }
    public UUID getCreadorId() { return creadorId; }
    public String getCreadorProfileImage(){return creadorProfileImage;}
}