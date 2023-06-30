package ru.senya.sampleserv.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.senya.sampleserv.models.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static ru.senya.sampleserv.utils.Utils.*;

@RestController
public class MainController {

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file==null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is empty");
        }
        Model model = null;
        String uniqueFilename = UUID.randomUUID() + ".jpeg";
        String path = PATH_FOLDER + uniqueFilename;
        try {
            Files.copy(file.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            model = Model.builder()
                    .regularPath("http://" + SERVER_HOST + "/get/" + uniqueFilename)
                    .build();
            processImage(model, path, uniqueFilename);
        } catch (IOException ignored) {
        }
        return ResponseEntity.ok(model);
    }

    @GetMapping(value = "/get/{imageName:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Resource getImage(@PathVariable String imageName) {
        return new FileSystemResource(PATH_FOLDER + imageName);
    }
}
