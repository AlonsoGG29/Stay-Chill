package com.aka.staychill.types;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class AsistenciaEvento {
    @SerializedName("usuario_id")
    private UUID usuarioId;

    // coincide con el alias de join en el select
    @SerializedName("usuarios")
    private Usuario usuario;

    public UUID getUsuarioId() { return usuarioId; }
    public Usuario getUsuario()    { return usuario;    }
}
