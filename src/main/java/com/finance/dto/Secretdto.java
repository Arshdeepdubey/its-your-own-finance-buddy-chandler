package com.finance.dto;

public class Secretdto {
    private String name;
    private String value;

    public Secretdto() {}

    public Secretdto(String name, String value) {
        this.name=name;
        this.value=value;
    }

    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value=value;
    }
}
