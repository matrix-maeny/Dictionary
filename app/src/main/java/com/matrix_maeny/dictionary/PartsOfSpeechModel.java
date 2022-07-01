package com.matrix_maeny.dictionary;

import java.util.HashMap;

public class PartsOfSpeechModel {

    private String partsOfSpeech;
    private String definitions;

    public PartsOfSpeechModel(String partsOfSpeech, String definitions) {
        this.partsOfSpeech = partsOfSpeech;
        this.definitions = definitions;
    }

    public String getDefinitions() {
        return definitions;
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    public String getPartsOfSpeech() {
        return partsOfSpeech;
    }

    public void setPartsOfSpeech(String partsOfSpeech) {
        this.partsOfSpeech = partsOfSpeech;
    }


}
