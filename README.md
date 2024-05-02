# Сar-number-detection
Проект реализуется в рамках курса по глубокому обучению.



# Задача
Задача стояла в следующем:
Существует предприятие, директора интересуют автомобильные номера. Есть камера, она шлет поток, мы из него выделяем фото. Распознаем:
<ul>
<li>Российские номера</li>
<li>Тип автомобиля</li>
<li>Цвет автомобиля</li>
<li>Кто из работников уезжает домой пораньше</li>
</ul>
Важно запоминать в какое время приехала машина, не обязательно сохранять сами данные о машине и сотруднике и ее номера.

# Quick Start

Чтобы запустить проект локально, необходимо:
1. Создать виртуальное окружение
2. Установить зависимости из requirements.txt:
```
pip install -r requirements.txt
```
3. Проверить пути до весов/видео
4. ``` python main.py ```

# Решение
Для распознавания автомобиля по типам и номерного знака на изображении была дообучена YOLOv5.
Пайплайн выглядит следующим образом:
1. Сначала определяется, что бокс номера лежит внутри бокса машины, тогда номерной знак присваивается этой машине
2. Записывается тип машины и координаты номера и бокса автомобиля
3. Затем происходит распознавание номера машины и проверка распознанного номера по регулярному выражению
4. Производится распознование цвета автомобиля

## Пример распознавания:
**_При запуске программы можно остановить ее, нажав на клавишу "q", также можно остановиться на определенном фрейме на пять секунд во время возпроизведения видео, нажав на клавишу "s"._**

<br>

**Производительность алгоритма:**
<p>На CPU алгоритм обрабатывает 2.7577 изображения в секунду (FPS). Эксперименты проводились на компьютере с 2,2 GHz Intel Core i7</p>

<br>
<img width="1080" alt="Снимок экрана 2022-10-09 в 13 36 12" src="https://user-images.githubusercontent.com/27068383/194752096-7be94ab1-7f43-4a9b-9314-9ff58d9016a6.png">

<img width="1080" alt="Снимок экрана 2022-10-09 в 13 35 48" src="https://user-images.githubusercontent.com/27068383/194752086-e0f2957b-a509-46d9-a11d-eebafa9f8725.png">



## Распознавание номера
Для распознавания номера на номерных знаках сначала использовался tesseract, но точность получилась низкая:
<br>
![2022-10-05 16 55 17](https://user-images.githubusercontent.com/27068383/194079080-494d75e1-ec2c-44c9-9404-e665737329ff.jpg)
<br>
<p>После этого было принято решение обучить нейросеть архитектуры LPRnet. Точность распознавания текста 89,6%.</p>

## Детекция цвета

<p>Для распознавания цвета каждый бокс машины сначала разбивался на вектора трех цветов: красный, синий, зеленый (с помощью метода OpenCV split()). Затем брались максимумы из каждого из векторов и значение максимума записывалось в общее значение цвета изображения в формате BGR (то есть максимальное значение из синего, из зеленого и из красного записывалось в общее значение).</p>
<p>Затем то же самое производится с изображениями из обучающего датасета (директория training_data). Каждое из изображений находится в папке соответствующего цвета (так происходит разметка датасета). Таким образом, в файл training.data сохраняются значения BGR для каждого изображения из обучающей выборки и тот цвет, который представлен на изображении.</p>
<p>В файл test.data сохраняется значение BGR для изображения, цвет которого мы детектируем. Затем с применением KNN-классификации определяется, какой цвет у машины.</p>

## Дерево проекта

```bash

├── colour_detection
│   ├── detect_color.py
│   └── training_dataset
│       ├── black
│       │   ├── black1.png
│       │   ├── black10.png
│       │   ...
│       ├── blue
│       │   ├── blue.jpg
│       │   ├── blue1.jpg
│       │   ...
│       ├── green
│       │   ├── green1.jpg
│       │   ...
│       ├── orange
│       │   ├── orange1.png
│       │   ├── orange10.png
│       │   ...
│       ├── red
│       │   ├── red1.jpg
│       │   ├── red10.jpg
│       │   ...
│       ├── violet
│       │   ├── violet1.png
│       │   ├── violet10.png
│       │   ...
│       ├── white
│       │   ├── white1.png
│       │   ├── white10.jpg
│       │   ...
│       └── yellow
│           ├── yellow1.jpg
│           ├── yellow2.png
│           ...
├── lpr_net
│   ├── data
│   │   ├── NotoSansCJK-Regular.ttc
│   │   ├── __init__.py
│   │   └── load_data.py
│   ├── model
│   │   ├── __init__.py
│   │   ├── lpr_net.py
│   │   └── weights
│   │       ├── Final_LPRNet_model.pth
│   │       └── LPRNet__iteration_2000_28.09.pth
│   └── rec_plate.py
├── main.py
├── object_detection
│   ├── YOLOS_cars.pt
│   └── detect_car_YOLO.py
├── requirements.txt
├── settings.py
├── test
│   └── videos
│       ├── test.mp4
│       └── test2.mp4
└── track_logic.py

```

# Датасет

Датасет состоит из 4 классов:

- 0 - номера автомобилей
- 1 - легковые автомобили
- 2 - грузовые автомобили
- 3 - общественный транспорт

### Train/test split

**В обучающей выборке:** 1200 изображений (92%)

**В тестовой выборке:** 103 изображения (8%)


# Описание обученных моделей
В задаче распознавания объектов на изображении были проведены эксперименты с обучением моделей на сетях с разной архитектурой. Все гиперпараметры в YOLO изначально подобраны оптимальным образом, мы меняли только архитектуру, чтобы получить наиболее точную.

## Модель YOLOv5n


|  Класс | mAP50 | mAP50-95 |
| --- | --- | --- |
| Для всех | 0.855 | 0.708 |
| 0 (Номерные знаки) | 0.907 | 0.641 |
| 1 (Легковые автомобили) | 0.851 | 0.76 |
| 2 (Грузовые автомобили) | 0.87 | 0.75 |
| 3 (Общественный транспорт) | 0.792 | 0.682 |


## Модель YOLOv5s

|  Класс | mAP50 | mAP50-95 |
| --- | --- | --- |
| Для всех | 0.807 | 0.709 |
| 0 (Номерные знаки) | 0.91 | 0.691 |
| 1 (Легковые автомобили) | 0.827 | 0.786 |
| 2 (Грузовые автомобили) | 0.777 | 0.723 |
| 3 (Общественный транспорт) | 0.712 | 0.635 |

## Модель YOLOv5m
Модель была обучена на 19 эпохах, так как размер датасета был слишком маленьким для сети такого размера.


|  Класс | mAP50 | mAP50-95 |
| --- | --- | --- |
| Для всех | 0.848 | 0.726 |
| 0 (Номерные знаки) | 0.932 | 0.69 |
| 1 (Легковые автомобили) | 0.859 | 0.795 |
| 2 (Грузовые автомобили) | 0.839 | 0.762 |
| 3 (Общественный транспорт) | 0.761 | 0.658 |


# Reference

[YOLOv5](https://github.com/ultralytics/yolov5?ysclid=l9187ounp212699888)
<br>
[COLOR RECOGNITION](https://github.com/ahmetozlu/color_recognition) 
<br>
[NUMBER DETECTION](https://github.com/sirius-ai/LPRNet_Pytorch)

