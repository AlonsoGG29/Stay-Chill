package com.aka.staychill;

import android.content.Context;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class EventoDeserializer implements JsonDeserializer<Evento> {
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public EventoDeserializer(Context context) {
        this.context = context;
    }

    @Override
    public Evento deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Campos básicos del evento
        Long id = jsonObject.get("id").getAsLong();
        String nombre = jsonObject.get("nombre_evento").getAsString();
        String localizacion = getNullableString(jsonObject, "localizacion");
        String descripcion = getNullableString(jsonObject, "descripcion");
        Date fecha = parseDate(jsonObject, "fecha_evento", dateFormat);
        Date hora = parseDate(jsonObject, "hora_evento", timeFormat);
        String tipoDeEvento = getNullableString(jsonObject, "tipo_de_evento");
        UUID creadorId = parseUUID(jsonObject, "creador_id");
        String creadorNombre = "";
        String creadorApellido = "";
        String creadorPais = "";

        // Imagen del evento (estática)
        String imagenNombre = getNullableString(jsonObject, "imagen_del_evento");
        int imagenDelEvento = 0;
        if (!imagenNombre.isEmpty()) {
            imagenDelEvento = this.context.getResources().getIdentifier(imagenNombre, "drawable", this.context.getPackageName());

        }

        // Foto de perfil del creador (desde el JOIN)
        String creadorProfileImage = "";
        if (jsonObject.has("usuarios") && !jsonObject.get("usuarios").isJsonNull()) {
            JsonObject usuarioJson = jsonObject.getAsJsonObject("usuarios");
            creadorProfileImage = getNullableString(usuarioJson, "profile_image_url");
            creadorNombre = getNullableString(usuarioJson, "nombre");
            creadorApellido = getNullableString(usuarioJson, "apellido");
            creadorPais = getNullableString(usuarioJson, "pais");
        }

        return new Evento(
                id,
                nombre,
                localizacion,
                descripcion,
                fecha,
                hora,
                tipoDeEvento,
                imagenDelEvento,
                creadorId,
                creadorProfileImage,
                creadorNombre,
                creadorApellido,
                creadorPais
        );
    }

    // Métodos auxiliares para simplificar el código
    private String getNullableString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsString()
                : "";
    }

    private Date parseDate(JsonObject json, String key, SimpleDateFormat format) {
        try {
            return json.has(key) && !json.get(key).isJsonNull()
                    ? format.parse(json.get(key).getAsString())
                    : null;
        } catch (ParseException e) {
            return null;
        }
    }

    private UUID parseUUID(JsonObject json, String key) {
        try {
            return json.has(key) && !json.get(key).isJsonNull()
                    ? UUID.fromString(json.get(key).getAsString())
                    : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

