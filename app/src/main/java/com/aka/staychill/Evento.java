package com.aka.staychill;

import java.time.OffsetTime;
import java.util.Date;

public class Evento {

    private final String nombre;
    private final String localizacion;
    private final String descripcion;
    private final Date fecha; // Puedes cambiarlo a LocalDate si es mejor para ti
    private final OffsetTime hora; // Ahora maneja timetz correctamente

    public Evento(String nombre, String localizacion, String descripcion, Date fecha, OffsetTime hora) {
        this.nombre = nombre;
        this.localizacion = localizacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
    }

    public OffsetTime getHora() {
        return hora;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Date getFecha() {
        return fecha;
    }



}