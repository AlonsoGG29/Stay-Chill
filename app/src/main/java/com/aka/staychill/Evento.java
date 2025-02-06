package com.aka.staychill;

public class Evento {

    //Clase que se comunica con MiAdaptador y le pasa la información

    private final String imageUrl;
    private final String text;
    private final String imageUrl2;

    public Evento(String imageUrl, String text, String imageUrl2) {
        this.imageUrl = imageUrl;
        this.text = text;
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }
}
