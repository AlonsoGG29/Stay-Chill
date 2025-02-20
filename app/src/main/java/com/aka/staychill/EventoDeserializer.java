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
    public Evento deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext jsonContext) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String nombre = jsonObject.get("nombre_evento").getAsString();
        String localizacion = jsonObject.has("localizacion") && !jsonObject.get("localizacion").isJsonNull() ? jsonObject.get("localizacion").getAsString() : "";
        String descripcion = jsonObject.has("descripcion") && !jsonObject.get("descripcion").isJsonNull() ? jsonObject.get("descripcion").getAsString() : "";

        Date fecha = null;
        if(jsonObject.has("fecha_evento") && !jsonObject.get("fecha_evento").isJsonNull()){
            try {
                fecha = dateFormat.parse(jsonObject.get("fecha_evento").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Date hora = null;
        if(jsonObject.has("hora_evento") && !jsonObject.get("hora_evento").isJsonNull()){
            try {
                hora = timeFormat.parse(jsonObject.get("hora_evento").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String tipoDeEvento = jsonObject.has("tipo_de_evento") && !jsonObject.get("tipo_de_evento").isJsonNull() ? jsonObject.get("tipo_de_evento").getAsString() : "";

        // Convertir el nombre de la imagen en el id del recurso drawable.
        String imagenNombre = jsonObject.has("imagen_del_evento") && !jsonObject.get("imagen_del_evento").isJsonNull() ? jsonObject.get("imagen_del_evento").getAsString() : "";
        int imagenDelEvento = 0;
        if(!imagenNombre.isEmpty()){
            imagenDelEvento = context.getResources().getIdentifier(imagenNombre, "drawable", context.getPackageName());
        }

        UUID creadorId = null;
        if(jsonObject.has("creador_id") && !jsonObject.get("creador_id").isJsonNull()){
            creadorId = UUID.fromString(jsonObject.get("creador_id").getAsString());
        }

        return new Evento(nombre, localizacion, descripcion, fecha, hora, tipoDeEvento, imagenDelEvento, creadorId);
    }
}
