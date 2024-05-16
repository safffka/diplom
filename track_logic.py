import re
import numpy

def check_numbers_overlaps(labls_cords: dict) -> list:
    """
    Check each number's BB and correlate it with car's BB
    return: list - the list has following structure [
        [(number's cords), (car's cords), 'car_type'],
        [(number's cords), (car's cords), 'car_type'],
        ...
        ]
    """
    new_cars = []

    for number in labls_cords["numbers"]:
        for car in labls_cords["cars"]:
            # check if number's bounding box fully overlaps car's
            if (car[0] <= number[0] <= number[2] <= car[2]) and (
                car[1] <= number[1] <= number[3] <= car[3]
            ):
                new_cars.append([number, car, "car"])

        for car in labls_cords["trucks"]:
            # check if number's bounding box fully overlaps car's
            if (car[0] <= number[0] <= number[2] <= car[2]) and (
                car[1] <= number[1] <= number[3] <= car[3]
            ):
                new_cars.append([number, car, "truck"])

        for car in labls_cords["buses"]:  # Corrected spelling from 'busses' to 'buses'
            # check if number's bounding box fully overlaps car's
            if (car[0] <= number[0] <= number[2] <= car[2]) and (
                car[1] <= number[1] <= number[3] <= car[3]
            ):
                new_cars.append([number, car, "bus"])

    return new_cars

def check_roi(coords):
    """
    Example function to check if the given coordinates are within a predefined 'region of interest'
    """
    # Example of a detection area (You should adjust this based on your actual needs)
    detection_area = [(100, 200), (2920, 500)]  # Example coordinates

    x_center = (coords[0] + coords[2]) / 2
    y_center = (coords[1] + coords[3]) / 2

    if detection_area[0][0] <= x_center <= detection_area[1][0] and detection_area[0][1] <= y_center <= detection_area[1][1]:
        return True
    else:
        return False
