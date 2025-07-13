package com.jpmc.midascore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;

@RestController
public class BalanceController {

    private final UserRepository users;

    public BalanceController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") long userId) {
        UserRecord user = users.findById(userId);
        float bal = (user != null) ? user.getBalance() : 0f;
        return new Balance(bal);
    }
}
