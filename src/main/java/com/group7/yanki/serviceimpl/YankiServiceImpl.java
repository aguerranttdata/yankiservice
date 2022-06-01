package com.group7.yanki.serviceimpl;

import com.group7.yanki.dto.AccountResponse;
import com.group7.yanki.dto.LinkRequest;
import com.group7.yanki.dto.Result;
import com.group7.yanki.exception.account.AccountNotFoundException;
import com.group7.yanki.model.Yanki;
import com.group7.yanki.repository.YankiRepository;
import com.group7.yanki.service.AccountService;
import com.group7.yanki.service.YankiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class YankiServiceImpl implements YankiService {

    @Autowired
    private YankiRepository yankiRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MessageServiceImpl messageService;

    @Override
    public Flux<Yanki> getByPhone(Long phone) {
        return yankiRepository.findYankiByFromOrTo(phone, phone)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Not found phone:" + phone)))
                .doOnError(ex -> log.error("Not found phone: {}", phone, ex));
    }

    @Override
    public Mono<String> makeYanki(Yanki yanki) {
        return accountService.getByPhone(yanki.getFrom())
                .flatMap(accountFrom -> {
                    log.info("Account from:{}", accountFrom);
                    if (!Objects.isNull(accountFrom.getDebitCard())) {
                        return accountService.getByPhone(yanki.getTo())
                                .then(Mono.just(messageService.sendToAccount(yanki)));
                    }

                    if (accountFrom.getBalance() < yanki.getAmount()) {
                        return Mono.just(messageService.sendResult(Result.builder()
                                .to(accountFrom.getPhone())
                                .status("Failed")
                                .message("Not enough money")
                                .build()));
                    }

                    return accountService.updateAmountByPhone(accountFrom.getPhone(), -yanki.getAmount())
                            .then(Mono.just(messageService.sendResult(Result.builder()
                                    .to(accountFrom.getPhone())
                                    .status("Success")
                                    .message("You make a Yanki of " + yanki.getAmount() + " to " + yanki.getTo())
                                    .build())))
                            .then(accountService.getByPhone(yanki.getTo()))
                            .flatMap(accountTo -> {
                                if (!Objects.isNull(accountTo.getDebitCard())) {
                                    return Mono.just(messageService.sendToAccount(yanki));
                                }

                                return accountService.updateAmountByPhone(accountTo.getPhone(), yanki.getAmount())
                                        .then(yankiRepository.save(yanki))
                                        .then(Mono.just(messageService.sendResult(Result.builder()
                                                .to(accountTo.getPhone())
                                                .status("Success")
                                                .message("You received a Yanki of " + yanki.getAmount() + " from " + yanki.getFrom())
                                                .build())));
                            });
                }).map(result -> result ? "Transferring..." : "Failed!");
    }

    @Override
    public Mono<String> linkYanki(LinkRequest linkRequest) {
        linkRequest.setState("request");
        return accountService.getByPhone(linkRequest.getPhone())
                .flatMap(accountResponse -> {
                    linkRequest.setAmount(accountResponse.getBalance());
                    return Mono.just(messageService.sendToLink(linkRequest));
                })
                .map(result -> result ? "Linking Yanki to debit card..." : "Failed!");
    }
}
