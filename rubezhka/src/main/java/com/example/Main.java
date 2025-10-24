package com.example;


// Пример использования:
public class Main {
    public static void main(String[] args) {
        double[] x = {0, 1, 2, 3, 4};
        double[] y = {20, 25, 22, 28, 30};
        CubicSpline spline = new CubicSpline(x, y);
        double value = spline.interpolate(1.5);
        System.out.println("Значение в точке 1.5: " + value);
    }
}
