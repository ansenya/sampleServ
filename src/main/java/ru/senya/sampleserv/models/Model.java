package ru.senya.sampleserv.models;

import lombok.*;

import java.util.concurrent.CountDownLatch;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Model {
    private String regularPath, aiPath, coloredPath, tags, hexColor;
    private CountDownLatch latch;
    public void await() throws InterruptedException {
        if (latch != null) {
            latch.await();
        }
    }
}
