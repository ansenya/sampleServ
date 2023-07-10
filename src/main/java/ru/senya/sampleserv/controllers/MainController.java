package ru.senya.sampleserv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.senya.sampleserv.models.Model;
import ru.senya.sampleserv.utils.Utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


import static ru.senya.sampleserv.utils.Utils.*;

@RestController
public class MainController {

    final
    Utils utils;

    public MainController(Utils utils) {
        this.utils = utils;
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is empty");
        }
        Model model = null;
        String uniqueFilename = UUID.randomUUID() + ".jpeg";
        String path = PATH_FOLDER + uniqueFilename;
        try {
            Files.copy(file.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            model = Model.builder()
                    .regularPath(uniqueFilename)
                    .build();
            CountDownLatch latch = new CountDownLatch(1);
            model.setLatch(latch);
            utils.processImages(model, latch, path, uniqueFilename);
            latch.await();
        } catch (IOException | InterruptedException ignored) {
        }
        return ResponseEntity.ok(model);
    }

    @GetMapping(value = "/get/{imageName:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Resource getImage(@PathVariable String imageName) {
        return new FileSystemResource(PATH_FOLDER + imageName);
    }

    @GetMapping("/test")
    public Model test() {
        return Model.builder()
                .aiPath("asd")
                .regularPath("123")
                .build();
    }
}
