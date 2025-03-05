package com.aka.staychill;

import com.google.gson.annotations.SerializedName;

public class Mensaje {
    @SerializedName("id")
    private String id;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("receiver_id")
    private String receiverId;

    @SerializedName("contenido")
    private String contenido;

    @SerializedName("fecha")
    private String fecha;

    // Getters y Setters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContenido() { return contenido; }
    public String getFecha() { return fecha; }
}