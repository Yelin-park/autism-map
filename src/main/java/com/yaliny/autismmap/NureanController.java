package com.yaliny.autismmap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NureanController {

    @GetMapping("/")
    public String root() {
        return "Nurean Server is running.";
    }
}
