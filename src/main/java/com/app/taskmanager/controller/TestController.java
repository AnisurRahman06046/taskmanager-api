package com.app.taskmanager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/testing")
public class TestController {
    


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
