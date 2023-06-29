## Сервер для обработки изображений

Эндпоинты: 
- `/process`
- `/get/{path}`
    
Порт: `8082`  

### Описание

Сервер предоставляет API для демонстрации работы нейронной сети по обработке изображений. Для использования его функциональности необходимо установить и запустить сервер, настроить библиотеку OpenCV и скачать веса модели YOLOV4.    
**Сервер плохо обрабатывает параллельные запросы (особенно на видеокарте)!!** 

### Методы

#### `POST /process`

Принимает файл изображения в теле запроса с именем `file`. Обрабатывает загруженное изображение с помощью нейронной сети и возвращает JSON-ответ с результатами обработки. Изображения сохраняются в папку `src/main/resources/static`.

Пример ответа:

```json
{
  "regularPath": "http://localhost:8082/get/4ef3ec92-4ae0-423b-9c22-62c39cd16a01.jpg",
  "aiPath": "http://localhost:8082/get/ai_4ef3ec92-4ae0-423b-9c22-62c39cd16a01.jpg",
  "coloredPath": "http://localhost:8082/get/colored_4ef3ec92-4ae0-423b-9c22-62c39cd16a01.jpg",
  "tags": "кот: 0,91 #98918B; ",
  "hexColor": "#C4C2C0"
}
```

- `regularPath`: Ссылка на исходное загруженное изображение.
- `aiPath`: Ссылка на обработанное изображение с нанесенными тегами.
- `coloredPath`: Ссылка на обработанное изображение с примененным цветом.
- `tags`: Теги, обнаруженные на изображении в формате "название: вероятность #цвет;". Может содержать несколько тегов, разделенных точкой с запятой.
- `hexColor`: HEX-код цвета, примененного к изображению.

### Запуск сервера
1. Установите и настройте окружение для запуска сервера, включая установку Java и Gradle. Либо скачайте IntelijiIDEA.
2. Склонируйте репозиторий сервера: `git clone https://github.com/ansenya/sampleServ.git`
3. Перейдите в каталог проекта sampleServ.
4. Скачайте [yolov4.weighs](https://github.com/AlexeyAB/darknet/releases/download/darknet_yolo_v3_optimal/yolov4.weights) в папку `src/main/config`.
5. Добавьте (или скачайте) [opencv-470.jar](https://drive.google.com/file/d/1NtxUHgiKtgyNM9bgBkAnuL8RbbN-XKz9/view?usp=sharing) в папку `src/main/resources/libs`.
6. Добавьте в конфигурацию `opencv-470.jar` в **project_structure** (если вы используете IntelijiIDEA) файл нативной библиотеки [libopencv_java470.so](https://drive.google.com/file/d/1wfKi149eeDpARoXjrKdwEXBng0eddxlF/view?usp=sharing).  
[`туториал`](https://drive.google.com/file/d/1mt4dAaLLfO7xRpDLP4znJf3hfKq-eJ4n/view?usp=sharing).
7. Запустите сервер. Сервер будет запущен на порту 8082.
  
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
