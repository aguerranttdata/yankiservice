package com.group7.yanki.controller;

import com.group7.yanki.dto.AccountRequest;
import com.group7.yanki.dto.AccountResponse;
import com.group7.yanki.dto.LinkRequest;
import com.group7.yanki.model.Yanki;
import com.group7.yanki.service.AccountService;
import com.group7.yanki.service.YankiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/yanki")
@AllArgsConstructor
@Slf4j
public class YankiController {
    @Autowired
    private YankiService service;

    @GetMapping("{phone}")
    public Flux<Yanki> getYankiByPhone(@PathVariable final Long phone) {
        return service.getByPhone(phone);
    }

    @PostMapping
    public Mono<String> makeYanki(@RequestBody final Yanki yanki) {
        return service.makeYanki(yanki);
    }

    @PostMapping("/link")
    public Mono<String> linkYanki(@RequestBody final LinkRequest linkRequest) {
        return service.linkYanki(linkRequest);
    }

}
