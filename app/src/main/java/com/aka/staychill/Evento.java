package com.aka.staychill;

import java.util.Date;
import java.time.OffsetTime;

public class Evento {
    private String nombre;
    private String localizacion;
    private String descripcion;
    private Date fecha;
    private String hora;;
    private String tipoDeEvento;
    private int imagenDelEvento;
    private String creadorId;

    public Evento(String nombre, String localizacion, String descripcion, Date fecha, String hora, String tipoDeEvento, int imagenDelEvento, String creadorId) {
        this.nombre = nombre;
        this.localizacion = localizacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoDeEvento = tipoDeEvento;
        this.imagenDelEvento = imagenDelEvento;
        this.creadorId = creadorId;
    }

    public String getNombre() { return nombre; }
    public String getLocalizacion() { return localizacion; }
    public String getDescripcion() { return descripcion; }
    public Date getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getTipoDeEvento() { return tipoDeEvento; }
    public int getImagenDelEvento() { return imagenDelEvento; }
    public String getCreadorId() { return creadorId; }
}
