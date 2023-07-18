package ru.senya.sampleserv.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static ru.senya.sampleserv.utils.Utils.SERVER_HOST;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"latch"})
public class Model {
    private String regularPath, aiPath, coloredPath, hexColor;
    private ArrayList<String> tags = new ArrayList<>();
    private CountDownLatch latch;

    public void await() throws InterruptedException {
        if (latch != null) {
            latch.await();
        }
    }

    public String getRegularPath() {
        return SERVER_HOST + "/get/" + regularPath;
    }

    public String getAiPath() {
        return SERVER_HOST + "/get/" + aiPath;
    }

    public String getColoredPath() {
        return SERVER_HOST + "/get/" + coloredPath;
    }
}
