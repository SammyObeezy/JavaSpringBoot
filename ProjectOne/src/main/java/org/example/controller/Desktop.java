package org.example.controller;

import org.springframework.stereotype.Component;

@Component
public class Desktop implements Computer {

    public void compile(){
        System.out.println("Compiling with 400 bugs");
    }
}
