package ru.senya.sampleserv.controllers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.senya.sampleserv.utils.Utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;

import static ru.senya.sampleserv.utils.Utils.PATH_FOLDER;

@Controller
public class HtmlController {

    final Utils utils;

    public HtmlController(Utils utils) {
        this.utils = utils;
    }


    @GetMapping("/form")
    public String form() {
        return "form";
    }

    @PostMapping("/pr")
    public String post_pr(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        ru.senya.sampleserv.models.Model model = null;
        String UUID = String.valueOf(java.util.UUID.randomUUID());
        String uniqueFilename = UUID + ".jpeg";
        String uniqueFilename2 = "ai_" + UUID + ".jpeg";
        String path = PATH_FOLDER + uniqueFilename;
        String path2 = PATH_FOLDER + uniqueFilename2;
        try {
            Files.copy(file.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(file.getInputStream(), Paths.get(path2), StandardCopyOption.REPLACE_EXISTING);
            model = ru.senya.sampleserv.models.Model.builder()
                    .regularPath(uniqueFilename)
                    .build();
            CountDownLatch latch = new CountDownLatch(1);
            model.setLatch(latch);
            utils.processImages2(model, latch, path, uniqueFilename);
            latch.await();
        } catch (IOException | InterruptedException ignored) {
        }

        Gson gson = new Gson().newBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getName().equals("latch");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        String json = gson.toJson(model);
        StringBuilder tags = new StringBuilder();
        assert model != null;
        tags.append("tags: \n");
        for (String s : model.getEnTags()) {
            tags.append(" - ").append(s).append("\n");
        }

        return "redirect:/pr?img=" + URLEncoder.encode(model.getAiPath(), StandardCharsets.UTF_8) + "&text=" + URLEncoder.encode(String.valueOf(tags), StandardCharsets.UTF_8);
    }


    @GetMapping("/pr")
    public String pr(Model model, @RequestParam("img") String img, @RequestParam("text") String text) {
        model.addAttribute("img", img);
        model.addAttribute("text", text);
        return "page";
    }

}
