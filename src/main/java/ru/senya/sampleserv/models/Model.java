package ru.senya.sampleserv.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static ru.senya.sampleserv.utils.Utils.SERVER_HOST;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"latch", "regularPath", "aiPath", "coloredPath", "success"})
@ToString
public class Model {
    private String regularPath, aiPath, coloredPath;
    @Builder.Default
    private String imageName = "";
    private double imageWidth, imageHeight;
    @Builder.Default
    private ArrayList<String> ruTags = new ArrayList<>();
    @Builder.Default
    private ArrayList<String> enTags = new ArrayList<>();
    private String text;
    private String hexColor;
    private Integer intColor;
    private double[] scalarColor;
    private CountDownLatch latch;
    private boolean success = false;

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

    public String toJson() {
        return "{" +
                "\\\"imageName\\\": " + "\\\"" + imageName + "\\\"" + ", " +
                "\\\"imageWidth\\\": " + imageWidth + ", " +
                "\\\"imageHeight\\\": " + imageHeight + ", " +
                "\\\"ruTags\\\": " + ruTags + ", " +
                "\\\"enTags\\\": " + enTags + ", " +
                "\\\"hexColor\\\": " + "\\\"" + hexColor + "\\\"" + ", " +
                "\\\"intColor\\\":" + intColor + ", " +
                "\\\"scalarColor\\\": " + Arrays.toString(scalarColor) +
                "}";
    }
}
