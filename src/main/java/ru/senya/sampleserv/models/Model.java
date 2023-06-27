package ru.senya.sampleserv.models;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Model {
    private String regularPath, aiPath, coloredPath, tags, hexColor;
}
