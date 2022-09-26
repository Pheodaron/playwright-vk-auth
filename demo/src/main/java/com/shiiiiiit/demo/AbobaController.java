package com.shiiiiiit.demo;

import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class AbobaController {

    private final VkService vkService;

    public AbobaController(VkService vkService) {
        this.vkService = vkService;
    }

    @GetMapping("/start")
    public ResponseEntity<String> parseGroup() {
        new VkAuthThread("authThread").start();
        System.out.println(Thread.currentThread().getName());

        return ResponseEntity.ok().body("aboba");
    }

    @GetMapping("/threads")
    public ResponseEntity<List<String>> getAllThreads() {
        var threads = Thread.getAllStackTraces().keySet().stream().map(Thread::getName).collect(Collectors.toList());

        return ResponseEntity.ok().body(threads);
    }

    @GetMapping("/code")
    public ResponseEntity<String> startCreadentialAuth(
            @RequestParam String code
    ) {
        try {
            vkService.enqueueValue(Pair.of("aboba", code));

            return ResponseEntity.ok().body("Success");
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().body("Non success!");
        }
    }
}
