package ru.senya.sampleserv.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import java.util.concurrent.CountDownLatch;


import static ru.senya.sampleserv.utils.Utils.*;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    final
    Utils utils;

    public RestController(Utils utils) {
        this.utils = utils;
    }

//    @PostMapping("/process")
//    public ResponseEntity<?> process(@RequestParam(value = "file", required = false) MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            return ResponseEntity.badRequest().body("file is empty");
//        }
//        Model model = null;
//        String uniqueFilename = UUID.randomUUID() + ".jpeg";
//        String path = PATH_FOLDER + uniqueFilename;
//        try {
//            Files.copy(file.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
//            model = Model.builder()
//                    .regularPath(uniqueFilename)
//                    .build();
//            CountDownLatch latch = new CountDownLatch(1);
//            model.setLatch(latch);
//            utils.processImages(model, latch, path, uniqueFilename);
//            latch.await();
//        } catch (IOException | InterruptedException ignored) {
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Access-Control-Allow-Origin", "*");
//
//        return ResponseEntity
//                .ok()
//                .headers(headers)
//                .body(model);
//    }

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

    @PostMapping("/process")
    public ResponseEntity<?> process2(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is empty");
        }
        Model model = null;
        String UUID = String.valueOf(java.util.UUID.randomUUID());
        String uniqueFilename = UUID + ".jpeg";
        String uniqueFilename2 = "ai_"+UUID+".jpeg";
        String path = PATH_FOLDER + uniqueFilename;
        String path2 = PATH_FOLDER + uniqueFilename2;
        try {
            Files.copy(file.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(file.getInputStream(), Paths.get(path2), StandardCopyOption.REPLACE_EXISTING);
            model = Model.builder()
                    .regularPath(uniqueFilename)
                    .build();
            CountDownLatch latch = new CountDownLatch(1);
            model.setLatch(latch);
            model.setImageName(file.getOriginalFilename());
            utils.processImages2(model, latch, path, uniqueFilename);
            latch.await();
        } catch (Exception e) {
            return ResponseEntity.status(418).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Origin", "*");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(model);
    }
}
