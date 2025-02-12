package com.example.kahootmaster;

import java.util.List;

public class Question {
    private int id;
    private String texto;
    private List<String> respuestas;
    private int respuestaCorrecta;
    private boolean canAnswer;

    public Question() {
        // Constructor vacío necesario para Firebase
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public List<String> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<String> respuestas) {
        this.respuestas = respuestas;
    }

    public int getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public void setRespuestaCorrecta(int respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta;
    }

    public boolean isCanAnswer() {
        return canAnswer;
    }

    public void setCanAnswer(boolean canAnswer) {
        this.canAnswer = canAnswer;
    }
}