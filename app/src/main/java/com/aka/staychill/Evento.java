package com.aka.staychill;

import java.util.Date;
import java.util.UUID;

public class Evento {

    private Long id;
    private String nombre;
    private String localizacion;
    private String descripcion;
    private Date fecha;
    private Date hora;
    private String tipoDeEvento;
    private int imagenDelEvento;
    private UUID creadorId;
    private String creadorProfileImage;
    private String creadorNombre;
    private String creadorApellido;
    private String creadorPais;

    public Evento(Long id, String nombre, String localizacion, String descripcion, Date fecha, Date hora,
                  String tipoDeEvento, int imagenDelEvento, UUID creadorId, String creadorProfileImage,
                  String creadorNombre, String creadorApellido, String creadorPais) {
        this.id = id;
        this.nombre = nombre;
        this.localizacion = localizacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoDeEvento = tipoDeEvento;
        this.imagenDelEvento = imagenDelEvento;
        this.creadorId = creadorId;
        this.creadorProfileImage = creadorProfileImage;
        this.creadorNombre = creadorNombre;
        this.creadorApellido = creadorApellido;
        this.creadorPais = creadorPais;
    }


    // Getters
    public Long getIdEvento(){return id;}
    public String getNombre() { return nombre; }
    public String getLocalizacion() { return localizacion; }
    public String getDescripcion() { return descripcion; }
    public Date getFecha() { return fecha; }
    public Date getHora() { return hora; }
    public String getTipoDeEvento() { return tipoDeEvento; }
    public int getImagenDelEvento() { return imagenDelEvento; }
    public UUID getCreadorId() { return creadorId; }
    public String getCreadorProfileImage(){return creadorProfileImage;}
    public String getCreadorNombre() { return creadorNombre; }
    public String getCreadorApellido() { return creadorApellido; }
    public String getCreadorPais() { return creadorPais; }

}
