package com.aka.staychill;

public class Evento {

    //Clase que se comunica con MiAdaptador y le pasa la información

    private String imageUrl;
    private String text;
    private String secondaryText;

    public Evento(String imageUrl, String text, String secondaryText) {
        this.imageUrl = imageUrl;
        this.text = text;
        this.secondaryText = secondaryText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getSecondaryText() {
        return secondaryText;
    }
}
