package com.jpmc.midascore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

@Component
public class TransactionListener {

    private final UserRepository users;
    private final TransactionRepository txs;
    private final RestTemplate rest;

    @Value("${incentive.api.url:http://localhost:8080/incentive}")
    private String incentiveUrl;

    @Autowired
    public TransactionListener(UserRepository users,
            TransactionRepository txs,
            RestTemplate rest) {
        this.users = users;
        this.txs = txs;
        this.rest = rest;
    }

    @KafkaListener(topics = "${general.kafka-topic}")
    public void onMessage(Transaction t) {
        UserRecord sender = users.findById(t.getSenderId());
        UserRecord recipient = users.findById(t.getRecipientId());

        if (sender != null && recipient != null
                && sender.getBalance() >= t.getAmount()) {

            // 1) Call incentive API
            Incentive inc = rest.postForObject(incentiveUrl, t, Incentive.class);

            // 2) Persist transaction + incentive
            double incentiveAmt = (inc != null ? inc.getAmount() : 0.0);
            txs.save(new TransactionRecord(sender, recipient,
                    t.getAmount(), incentiveAmt));

            float amount = (float) t.getAmount();
            float incAmt = (float) incentiveAmt;

            // 3) Adjust balances:
            sender.setBalance(sender.getBalance() - amount);
            recipient.setBalance(
                    recipient.getBalance()
                    + amount
                    + incAmt
            );

            users.save(sender);
            users.save(recipient);

            // debug‐print Wilbur (or Waldorf) if you like:
            UserRecord w = users.findByName("wilbur");
            System.out.println("Wilbur’s balance = "
                    + Math.floor(w.getBalance()));
        }
    }
}
