package com.star.es.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hejx
 */
@RestController
@RequestMapping("/es/sync")
public class DataSyncController {

    @GetMapping("/product/{name}")
    public String hello(@PathVariable(value = "name") String name) {
        return "sync " + name;
    }
}
