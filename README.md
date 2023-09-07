## Сервер для обработки изображений

Эндпоинты (один): 
- `/process`
    
Порт: `8082`  

### Описание

Сервер предоставляет API для демонстрации работы нейронных сети по обработке, классификации... и прочему.  
Используется yolov4, yolo9000, efnet и YandexGPT.

### Методы

#### `POST /process`

Принимает файл изображения в теле запроса с именем `file`.
Обрабатывает загруженное изображение при помощи нейронных сетей и возвращает JSON-ответ с результатами обработки. 
Изображения сохраняются в папку `src/main/resources/static`.  
Папку иногда нужно чистить, сама она пока не очищается.

Пример ответа на [эту фотографию](https://drive.google.com/file/d/1Dh59MrVV0VmDPdkDpon-4EzaZXVbc3nz/view?usp=sharing):

```json
{
  "imageName": "🍓.jpeg",
  "imageWidth": 828.0,
  "imageHeight": 766.0,
  "ruTags": [
    "машина",
    "спортивный автомобиль, спортивная машина",
    "автомобиль"
  ],
  "enTags": [
    "car",
    "sports car, sport car"
  ],
  "text": "На этой фотографии мы видим спортивный автомобиль, который выглядит очень мощно и динамично. Он имеет ярко-красный цвет и черные диски на колесах, что придает ему еще больше стиля и элегантности.\n\nНа заднем плане можно увидеть городской пейзаж с высотными зданиями и дорогами, что создает ощущение скорости и свободы. Этот автомобиль, несомненно, привлечет внимание всех любителей спортивных машин и автомобилей в целом.\n\nТекст описания фотографии может быть следующим:\n\n\"Эта фотография представляет собой спортивный автомобиль красного цвета с черными дисками на колесах. Он выглядит очень мощно и динамично, создавая ощущение скорости и свободы. На заднем плане можно увидеть городской пейзаж с высотными зданиями, что добавляет еще больше атмосферы. Этот автомобиль идеально подойдет для тех, кто любит спортивные машины и автомобили в целом.\"",
  "hexColor": "#5D4641",
  "intColor": 6112833,
  "scalarColor": [
    65.0,
    70.0,
    93.0,
    0.0
  ]
}
```

### Запуск сервера
1. Установите и настройте окружение для запуска сервера, включая установку Java и Gradle. Либо скачайте IntelijiIDEA.
2. Склонируйте репозиторий сервера: `git clone https://github.com/ansenya/sampleServ.git`
3. Перейдите в каталог проекта sampleServ.
4. Скачайте [yolov4.weighs](https://github.com/AlexeyAB/darknet/releases/download/darknet_yolo_v3_optimal/yolov4.weights) в папку `src/main/config/yolo/`.
5. Скачайте [yolo9000.weighs](https://drive.google.com/file/d/1-lVakJPz4RaegjP70n2yWK1nxubQIQjX/view?usp=sharing) в папку `src/main/config/yolo9000/`.
6. Добавьте (или скачайте) [opencv-470.jar](https://drive.google.com/file/d/1NtxUHgiKtgyNM9bgBkAnuL8RbbN-XKz9/view?usp=sharing) в папку `src/main/resources/libs`.
7. Добавьте в конфигурацию `opencv-470.jar` в **project_structure** (если вы используете IntelijiIDEA) файл нативной библиотеки [libopencv_java470.so](https://drive.google.com/file/d/1wfKi149eeDpARoXjrKdwEXBng0eddxlF/view?usp=sharing).  
[`туториал`](https://drive.google.com/file/d/1mt4dAaLLfO7xRpDLP4znJf3hfKq-eJ4n/view?usp=sharing).
8. Запустите сервер. Сервер будет запущен на порту 8082.
  
Теперь вы можете отправить POST-запрос на http://localhost:8082/process, указав файл изображения в теле запроса с именем `file`. Получите ответ с результатами обработки изображения.



## Сборка OpenCV для работы с YOLOv4 в Java
### Сборка для работы на процессоре:
1. Склонируйте репозиторий OpenCV с GitHub по следующей ссылке: https://github.com/opencv/opencv/archive/refs/tags/4.7.0.zip.
2. Создайте папку, например, с названием "build", внутри каталога OpenCV, где будет компилироваться библиотека.
3. Откройте терминал в этой папке и выполните команду `cmake ..`
4. Затем выполните команду `make -j<N>`, где `<N>` - количество потоков процессора в системе.
5. После успешной компиляции библиотека будет собрана.  
### Сборка для работы на видеокарте:
1. Убедитесь, что у вас установлены CUDA и cuDNN. 
2. Склонируйте репозиторий OpenCV с GitHub по следующей ссылке: https://github.com/opencv/opencv/archive/refs/tags/4.7.0.zip.
3. Склонируйте дополнительные компоненты OpenCV, необходимые для компиляции с поддержкой CUDA, по ссылке: https://github.com/opencv/opencv_contrib/archive/refs/tags/4.7.0.zip.
4. Создайте папку, например, с названием "build", внутри каталога OpenCV, где будет компилироваться библиотека.
5. Откройте терминал в этой папке и выполните следующую команду:
`cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_CUDA=ON -D ENABLE_FAST_MATH=1 -D CUDA_FAST_MATH=1 -D WITH_CUBLAS=1 -D WITH_TBB=ON -D BUILD_opencv_java=ON -D BUILD_opencv_python3=OFF -D BUILD_opencv_python2=OFF -D BUILD_TESTS=ON -D BUILD_PERF_TESTS=ON -D OPENCV_EXTRA_MODULES_PATH=<path>/modules .. `  
Здесь `<path>` - путь к каталогу, где находятся дополнительные компоненты OpenCV (`opencv_contrib`).
6. Затем выполните команду `make -j<N>`, где `<N>` - количество потоков процессора в системе.
7. После успешной компиляции библиотека будет собрана.

`opencv-470.jar` находится в папке `../build/bin/`  
`libopencv_java470.so` находится в папке `../build/lib/`
