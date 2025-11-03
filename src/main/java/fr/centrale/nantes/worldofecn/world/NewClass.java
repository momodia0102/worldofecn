/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.centrale.nantes.worldofecn.world;
import java.util.Scanner;

/**
 *
 * @author Mohamed
 */
public class NewClass {

    public static int fibo(int n) {
        int res;
        int fibo1 = 1;
        int fibo2 = 1;

        res = 1;
        if (n <= 0) { res = 0; }
        if (n == 1 || n == 2) { res = 1; }
        for (int i = 3; i <= n; i++) {
            res = fibo1 + fibo2;
            fibo1 = fibo2;
            fibo2 = res;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println("Entrez un nombre : ");
        Scanner sc = new Scanner(System.in);
        int fiboNb = sc.nextInt();
        System.out.println("Fibo(" + fiboNb + ") = " + fibo(fiboNb));
    }
}
