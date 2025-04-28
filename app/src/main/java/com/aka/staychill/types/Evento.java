package com.aka.staychill.types;

import android.content.Context;

import com.aka.staychill.R;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Evento {

    @SerializedName("id")
    private Long id;

    @SerializedName("nombre_evento")
    private String nombreEvento;

    @SerializedName("localizacion")
    private String localizacion;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("fecha_evento")
    private String fechaStr;

    @SerializedName("hora_evento")
    private String horaStr;

    @SerializedName("tipo_de_evento")
    private String tipoDeEvento;

    @SerializedName("imagen_del_evento")
    private String imagenNombre;

    @SerializedName("numero_actual_participantes")
    private int numeroActualParticipantes;

    @SerializedName("limite_de_participantes")
    private int limiteParticipantes;

    @SerializedName("creador_id")
    private UUID creadorId;

    @SerializedName("usuarios")
    private Creador creador;

    @SerializedName("asistentes_eventos")
    private List<Asistencia> asistencias = new ArrayList<>();

    private static class Creador {
        @SerializedName("foren_uid")
        UUID id;

        @SerializedName("profile_image_url")
        String profileImage;

        @SerializedName("nombre")
        String nombre;

        @SerializedName("apellido")
        String apellido;

        @SerializedName("pais")
        String pais;

        public void setId(UUID id) { this.id = id; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public void setPais(String pais) { this.pais = pais; }
    }
    private static class Asistencia {
        @SerializedName("usuario_id")
        UUID usuarioId;
    }


    public void setId(Long id) { this.id = id; }
    public void setNombreEvento(String nombreEvento) { this.nombreEvento = nombreEvento; }
    public void setLocalizacion(String localizacion) { this.localizacion = localizacion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaStr(String fechaStr) { this.fechaStr = fechaStr; }
    public void setHoraStr(String horaStr) { this.horaStr = horaStr; }
    public void setTipoDeEvento(String tipoDeEvento) { this.tipoDeEvento = tipoDeEvento; }
    public void setImagenNombre(String imagenNombre) { this.imagenNombre = imagenNombre; }
    public void setLimiteParticipantes(int limiteParticipantes) { this.limiteParticipantes = limiteParticipantes;}
    public void setNumeroActualParticipantes(int numeroActualParticipantes){this.numeroActualParticipantes = numeroActualParticipantes;}
    public void setCreador(Creador creador) { this.creador = creador; }

    public void setCreadorId(UUID id) {
        if (creador == null) creador = new Creador();
        creador.setId(id);
    }

    public void setCreadorProfileImage(String profileImage) {
        if (creador == null) creador = new Creador();
        creador.setProfileImage(profileImage);
    }
    public List<UUID> getAsistentes() {
        List<UUID> ids = new ArrayList<>();
        for (Asistencia asistencia : asistencias) {
            ids.add(asistencia.usuarioId);
        }
        return ids;
    }

    public Long getId(){return id;}
    public String getNombreEvento() {return nombreEvento;}
    public String getLocalizacion(){return localizacion;}
    public String getDescripcion(){return descripcion;}
    public String getTipoDeEvento(){return tipoDeEvento;}
    public String getFechaStr(){return fechaStr;}
    public int getLimitePersonas(){return limiteParticipantes;}
    public int getNumeroActualParticipantes(){return numeroActualParticipantes;}

    public String getHoraStr() {
        try {

            SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date horaDate = originalFormat.parse(horaStr);


            SimpleDateFormat nuevoFormato = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return nuevoFormato.format(horaDate);
        } catch (ParseException e) {
            return horaStr;
        }
    }
    public int getImagenDelEvento(Context context) {
        int resId = context.getResources().getIdentifier(
                imagenNombre,
                "drawable",
                context.getPackageName()
        );
        return resId != 0 ? resId : R.drawable.img_default;
    }

    public UUID getCreadorId() {
        return creadorId;
    }
    public UUID getCreadorDatos() { return creador.id; }
    public String getCreadorProfileImage() { return creador.profileImage; }
    public String getCreadorNombre() { return creador.nombre; }
    public String getCreadorApellido() { return creador.apellido; }
    public String getCreadorPais() { return creador.pais; }

    public int getColorResId() {
        switch (tipoDeEvento) {
            case "Deporte":
                return R.color.deporte_color;
            case "Comida y Bebida":
                return R.color.comida_color;
            case "Cultura y Arte":
                return R.color.cultura_color;
            case "Música y Entretenimiento":
                return R.color.musica_color;
            case "Naturaleza y Aire Libre":
                return R.color.naturaleza_color;
            case "Fiestas y Social":
                return R.color.fiesta_color;
            case "Aprendizaje y Desarrollo":
                return R.color.aprendizaje_color;
            case "Gaming y Tecnología":
                return R.color.gaming_color;
            case "Mascotas y Animales":
                return R.color.mascota_color;
            case "Viajes y Escapadas":
                return R.color.viaje_color;
            case "Fotografía y Creatividad":
                return R.color.fotografia_color;
            case "Salud y Bienestar":
                return R.color.salud_color;
            case "Motor y Aventura":
                return R.color.motor_color;
            default:
                return R.color.red;
        }
    }

}