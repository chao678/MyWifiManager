package com.example.mywifimanager;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        int count = 5;
        for (int j = 0; j < count; j++) {
            for (int i = 0; i < count; i++) {
                if (j == 0) {
                    System.out.print("* ");
                } else if (j == count - 1) {
                    System.out.print("* ");
                } else {
//                    if (i == 0) {
//                        System.out.print("* ");
//                    } else if (i == count - 1) {
//                        System.out.print("* ");
//                    } else {
//                        System.out.print("  ");
//                    }
                    if (i == 2) {
                        System.out.print("* ");
                    } else {
                        System.out.print("  ");
                    }
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("*       **");
        System.out.println("*     *  *");
        System.out.println("*   *    *");
        System.out.println("* *      *");
        System.out.println("*        *");

        System.out.println();
        for (int k = 0; k < 5; k++) {
            for (int l = 0; l < k + 1; l++) {
                System.out.print("*");
            }
            System.out.println();
        }

        System.out.println();
        int a = 9;
        int m = (int)Math.ceil(a/2) - 1;
        int p = 1;

        for (int k = 0; k < Math.ceil(a/2); k++) {
            for (int i = 0; i < m; i++) {
                System.out.print(" ");
            }
            m--;
            for (int j = 0; j < p; j++) {
                System.out.print("*");
            }
            p+=2;
            System.out.println();
        }
    }
}