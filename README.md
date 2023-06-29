## Сервер для обработки изображений

Эндпоинты: 
- `/process`
- `get/{path}`
    
Порт: `8082`  

### Описание

Сервер предоставляет API для демонстрации работы нейронной сети по обработке изображений. Для использования его функциональности необходимо установить и запустить сервер.

### Методы

#### `POST /process`

Принимает файл изображения в теле запроса с именем `file`. Обрабатывает загруженное изображение с помощью нейронной сети и возвращает JSON-ответ с результатами обработки.

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

- regularPath: Ссылка на исходное загруженное изображение.
- aiPath: Ссылка на обработанное изображение с нанесенными тегами.
- coloredPath: Ссылка на обработанное изображение с примененным цветом.
- tags: Теги, обнаруженные на изображении в формате "название: вероятность #цвет;". Может содержать несколько тегов, разделенных точкой с запятой.
- hexColor: HEX-код цвета, примененного к изображению.

### Запуск сервера
1. Установите и настройте окружение для запуска сервера, включая установку Java и Gradle.
2. Склонируйте репозиторий сервера: git clone https://github.com/ansenya/sampleServ.git
3. Перейдите в каталог проекта sampleServ.
4. Запустите сервер. Сервер будет запущен на порту 8082.
