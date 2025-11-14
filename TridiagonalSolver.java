package com.example;
import java.util.Scanner;


public class TridiagonalSolver {
    /**
     * Решает СЛАУ с трехдиагональной матрицей методом прогонки
     *
     * Система имеет вид:
     * b[0]*x[0] + c[0]*x[1] = d[0]
     * a[i]*x[i-1] + b[i]*x[i] + c[i]*x[i+1] = d[i], для i = 1..n-2
     * a[n-1]*x[n-2] + b[n-1]*x[n-1] = d[n-1]
     *
     * @param a нижняя диагональ (размер n-1)
     * @param b главная диагональ (размер n)
     * @param c верхняя диагональ (размер n-1)
     * @param d правая часть (размер n)
     * @return решение системы x (размер n)
     */
    public static double[] solve(double[] a, double[] b, double[] c, double[] d) {
        int n = b.length;

        // Проверка размерностей
        if (a.length != n - 1 || c.length != n - 1 || d.length != n) {
            throw new IllegalArgumentException("Неверные размерности массивов");
        }

        // Массивы для прогоночных коэффициентов (прямой ход)
        double[] alpha = new double[n];
        double[] beta = new double[n];

        // Решение
        double[] x = new double[n];

        // ПРЯМОЙ ХОД (вычисление прогоночных коэффициентов)
        // Начальные значения
        alpha[0] = -c[0] / b[0];
        beta[0] = d[0] / b[0];

        // Вычисление alpha[i] и beta[i] для i = 1..n-1
        for (int i = 1; i < n; i++) {
            double denominator;

            if (i < n - 1) {
                // Для i = 1..n-2
                denominator = b[i] + a[i - 1] * alpha[i - 1];
                alpha[i] = -c[i] / denominator;
                beta[i] = (d[i] - a[i - 1] * beta[i - 1]) / denominator;
            } else {
                // Для последнего элемента (i = n-1)
                denominator = b[i] + a[i - 1] * alpha[i - 1];
                beta[i] = (d[i] - a[i - 1] * beta[i - 1]) / denominator;
            }

            // Проверка деления на ноль
            if (Math.abs(denominator) < 1e-10) {
                throw new ArithmeticException("Деление на ноль. Метод не применим.");
            }
        }

        // ОБРАТНЫЙ ХОД (вычисление решения)
        // Последний элемент
        x[n - 1] = beta[n - 1];

        // Вычисление x[i] для i = n-2..0
        for (int i = n - 2; i >= 0; i--) {
            x[i] = alpha[i] * x[i + 1] + beta[i];
        }

        return x;
    }

    /**
     * Проверяет решение, подставляя его в исходную систему
     */
    public static void checkSolution(double[] a, double[] b, double[] c, double[] d, double[] x) {
        int n = b.length;
        System.out.println("\nПроверка решения:");

        for (int i = 0; i < n; i++) {
            double leftSide = b[i] * x[i];

            if (i > 0) {
                leftSide += a[i - 1] * x[i - 1];
            }
            if (i < n - 1) {
                leftSide += c[i] * x[i + 1];
            }

            System.out.printf("Уравнение %d: %.6f = %.6f (разница: %.2e)\n",
                    i + 1, leftSide, d[i], Math.abs(leftSide - d[i]));
        }
    }

    /**
     * Выводит матрицу системы в наглядном виде
     */
    public static void printSystem(double[] a, double[] b, double[] c, double[] d) {
        int n = b.length;
        System.out.println("\nСистема уравнений:");

        for (int i = 0; i < n; i++) {
            StringBuilder equation = new StringBuilder();

            if (i > 0) {
                equation.append(String.format("%.2f*x[%d] + ", a[i - 1], i - 1));
            }

            equation.append(String.format("%.2f*x[%d]", b[i], i));

            if (i < n - 1) {
                equation.append(String.format(" + %.2f*x[%d]", c[i], i + 1));
            }

            equation.append(String.format(" = %.2f", d[i]));
            System.out.println(equation);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Калькулятор СЛАУ методом прогонки ===\n");

        // Ввод размерности
        System.out.print("Введите размерность системы n: ");
        int n = scanner.nextInt();

        if (n < 2) {
            System.out.println("Размерность должна быть >= 2");
            return;
        }

        // Выделение памяти
        double[] a = new double[n - 1]; // нижняя диагональ
        double[] b = new double[n];     // главная диагональ
        double[] c = new double[n - 1]; // верхняя диагональ
        double[] d = new double[n];     // правая часть

        // Ввод главной диагонали
        System.out.println("\nВведите элементы главной диагонали b[i] (" + n + " элементов):");
        for (int i = 0; i < n; i++) {
            System.out.print("b[" + i + "] = ");
            b[i] = scanner.nextDouble();
        }

        // Ввод верхней диагонали
        System.out.println("\nВведите элементы верхней диагонали c[i] (" + (n - 1) + " элементов):");
        for (int i = 0; i < n - 1; i++) {
            System.out.print("c[" + i + "] = ");
            c[i] = scanner.nextDouble();
        }

        // Ввод нижней диагонали
        System.out.println("\nВведите элементы нижней диагонали a[i] (" + (n - 1) + " элементов):");
        for (int i = 0; i < n - 1; i++) {
            System.out.print("a[" + i + "] = ");
            a[i] = scanner.nextDouble();
        }

        // Ввод правой части
        System.out.println("\nВведите элементы правой части d[i] (" + n + " элементов):");
        for (int i = 0; i < n; i++) {
            System.out.print("d[" + i + "] = ");
            d[i] = scanner.nextDouble();
        }

        // Вывод системы
        printSystem(a, b, c, d);

        try {
            // Решение системы
            double[] x = solve(a, b, c, d);

            // Вывод решения
            System.out.println("\n=== РЕШЕНИЕ ===");
            for (int i = 0; i < n; i++) {
                System.out.printf("x[%d] = %.6f\n", i, x[i]);
            }

            // Проверка
            checkSolution(a, b, c, d, x);

        } catch (Exception e) {
            System.err.println("\nОшибка: " + e.getMessage());
        }

        scanner.close();
    }
}
