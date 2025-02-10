package com.aka.staychill;

import java.util.ArrayList;
import java.util.List;

public class EventoManejador {

    private static List<Evento> eventos = new ArrayList<>();

    public static void addEvento(Evento evento) {
        eventos.add(evento);
    }

    public static List<Evento> getEventos() {
        return eventos;
    }
}