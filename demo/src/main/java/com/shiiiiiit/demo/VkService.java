package com.shiiiiiit.demo;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class VkService {
    private final BlockingQueue<Pair<String, String>> vkCodeQueue = new LinkedBlockingQueue<>();

    public Pair<String, String> getQueuedValue() throws InterruptedException {
        return this.vkCodeQueue.take();
    }

    public void enqueueValue(Pair<String, String> value) throws InterruptedException {
        this.vkCodeQueue.put(value);
    }
}
