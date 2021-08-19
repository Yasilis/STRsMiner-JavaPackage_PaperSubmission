package com.alimaddi.statisticaltest;


//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

import java.text.DecimalFormat;

/**
 * This does a Fisher Exact test.  The Fisher's Exact test procedure calculates an exact probability value
 * for the relationship between two dichotomous variables, as found in a two by two cross table. The program
 * calculates the difference between the data observed and the data expected, considering the given marginal
 * and the assumptions of the model of independence. It works in exactly the same way as the Chi-square test
 * for independence; however, the Chi-square gives only an estimate of the true probability value, an estimate
 * which might not be very accurate if the marginal is very uneven or if there is a small value (less than five)
 * in one of the cells.
 * <p>
 * It uses an array of factorials initialized at the beginning to provide speed.
 * There could be better ways to do this.
 *
 * @author Ed Buckler
 * @version $Id: FisherExact.java,v 1
 */

public class FisherExactTest
{
    private final double[] logFactorial;
    int maxSize;


    /**
     * constructor for FisherExact table
     *
     * @param maxSize is the maximum sum that will be encountered by the table (a+b+c+d)
     */
    public FisherExactTest(int maxSize)
    {
        this.maxSize = maxSize;
        logFactorial = new double[maxSize + 1];
        logFactorial[0] = 0.0;
        for (int i = 1; i <= this.maxSize; i++)
        {
            logFactorial[i] = logFactorial[i - 1] + Math.log(i);
        }
    }

    /**
     * calculates the P-value for this specific state
     *
     * @param a a is 11 index in the four cells in a 2x2 matrix
     * @param b b is 12 index in the four cells in a 2x2 matrix
     * @param c c is 21 index in the four cells in a 2x2 matrix
     * @param d d is 22 index in the four cells in a 2x2 matrix
     * @return the P-value
     */
    public final double getP(int a, int b, int c, int d)
    {
        int n = a + b + c + d;
        if (n > maxSize)
        {
            return Double.NaN;
        }
        double p;
        p = (logFactorial[a + b] + logFactorial[c + d] + logFactorial[a + c] + logFactorial[b + d]) - (logFactorial[a] + logFactorial[b] + logFactorial[c] + logFactorial[d] + logFactorial[n]);
        return Math.exp(p);
    }

    /**
     * Calculates the one-tail P-value for the Fisher Exact test.  Determines whether to calculate the right- or left-
     * tail, thereby always returning the smallest p-value.
     *
     * @param a a is 11 index in the four cells in a 2x2 matrix
     * @param b b is 12 index in the four cells in a 2x2 matrix
     * @param c c is 21 index in the four cells in a 2x2 matrix
     * @param d d is 22 index in the four cells in a 2x2 matrix
     * @return one-tailed P-value (right or left, whichever is smallest)
     */
    public final double getCumlativeP(int a, int b, int c, int d)
    {
        int min, i;
        int n = a + b + c + d;
        if (n > maxSize)
        {
            return Double.NaN;
        }
        double p = 0;
        p += getP(a, b, c, d);
        if ((a * d) >= (b * c))
        {
            min = Math.min(c, b);
            for (i = 0; i < min; i++)
            {
                p += getP(++a, --b, --c, ++d);
            }
        }
        if ((a * d) < (b * c))
        {
            min = Math.min(a, d);
            for (i = 0; i < min; i++)
            {
                double pTemp = getP(--a, ++b, ++c, --d);
                p += pTemp;
            }
        }
        return p;
    }

    /**
     * Calculates the right-tail P-value for the Fisher Exact test.
     *
     * @param a a is 11 index in the four cells in a 2x2 matrix
     * @param b b is 12 index in the four cells in a 2x2 matrix
     * @param c c is 21 index in the four cells in a 2x2 matrix
     * @param d d is 22 index in the four cells in a 2x2 matrix
     * @return one-tailed P-value (right-tail)
     */
    public final double getRightTailedP(int a, int b, int c, int d)
    {
        int min, i;
        int n = a + b + c + d;
        if (n > maxSize)
        {
            return Double.NaN;
        }
        double p = 0;

        p += getP(a, b, c, d);
        min = Math.min(c, b);
        for (i = 0; i < min; i++)
        {
            p += getP(++a, --b, --c, ++d);

        }
        return p;
    }

    /**
     * Calculates the left-tail P-value for the Fisher Exact test.
     *
     * @param a a is 11 index in the four cells in a 2x2 matrix
     * @param b b is 12 index in the four cells in a 2x2 matrix
     * @param c c is 21 index in the four cells in a 2x2 matrix
     * @param d d is 22 index in the four cells in a 2x2 matrix
     * @return one-tailed P-value (left-tail)
     */
    public final double getLeftTailedP(int a, int b, int c, int d)
    {
        int min, i;
        int n = a + b + c + d;
        if (n > maxSize)
        {
            return Double.NaN;
        }
        double p = 0;

        p += getP(a, b, c, d);
        min = Math.min(a, d);
        for (i = 0; i < min; i++)
        {
            double pTemp = getP(--a, ++b, ++c, --d);
            p += pTemp;
        }


        return p;
    }


    /**
     * Calculates the two-tailed P-value for the Fisher Exact test.
     * <p>
     * In order for a table under consideration to have its p-value included
     * in the final result, it must have a p-value less than the original table's P-value, i.e.
     * Fisher's exact test computes the probability, given the observed marginal
     * frequencies, of obtaining exactly the frequencies observed and any configuration more extreme.
     * By "more extreme," we mean any configuration (given observed marginals) with a smaller probability of
     * occurrence in the same direction (one-tailed) or in both directions (two-tailed).
     *
     * @param a a is 11 index in the four cells in a 2x2 matrix
     * @param b b is 12 index in the four cells in a 2x2 matrix
     * @param c c is 21 index in the four cells in a 2x2 matrix
     * @param d d is 22 index in the four cells in a 2x2 matrix
     * @return two-tailed P-value or NaN if the table sum exceeds the maxSize
     */
    public final double getTwoTailedP(int a, int b, int c, int d)
    {
        int min, i;
        int n = a + b + c + d;
        if (n > maxSize)
        {
            return Double.NaN;
        }
        double p = 0;

        double baseP = getP(a, b, c, d);

        int initialA = a, initialB = b, initialC = c, initialD = d;
        p += baseP;
        min = Math.min(c, b);
        for (i = 0; i < min; i++)
        {
            double tempP = getP(++a, --b, --c, ++d);
            if (tempP <= baseP)
            {
                p += tempP;
            }
        }

        // reset the values to their original so we can repeat this process for the other side
        a = initialA;
        b = initialB;
        c = initialC;
        d = initialD;

        min = Math.min(a, d);
        for (i = 0; i < min; i++)
        {
            double pTemp = getP(--a, ++b, ++c, --d);
            if (pTemp <= baseP)
            {
                p += pTemp;
            }
        }
        return p;
    }


    public static void main(String[] args)
    {

        int[][] argInts = new int[27][4];
        argInts[0] = new int[]{3011, 30, 2020, 20};
        argInts[1] = new int[]{3011, 30, 2020, 5};
        argInts[2] = new int[]{3011, 30, 2020, 60};
        argInts[3] = new int[]{1, 2, 0, 3};
        argInts[4] = new int[]{3, 1, 1, 3};
        argInts[5] = new int[]{1, 3, 3, 1};
        argInts[6] = new int[]{0, 1, 1, 0};
        argInts[7] = new int[]{1, 0, 0, 1};
        argInts[8] = new int[]{11, 0, 0, 6};
        argInts[9] = new int[]{10, 1, 1, 5};
        argInts[10] = new int[]{5, 6, 6, 0};
        argInts[11] = new int[]{9, 2, 2, 4};
        argInts[12] = new int[]{6, 5, 5, 1};
        argInts[13] = new int[]{8, 3, 3, 3};
        argInts[14] = new int[]{7, 4, 4, 2};

        argInts[15] = new int[]{2494, 5350, 4448, 6019};
        argInts[16] = new int[]{2494, 5350, 3651, 6874};
        argInts[17] = new int[]{2494, 5350, 3423, 6912};
        argInts[18] = new int[]{2494, 5350, 3857, 6765};
        argInts[19] = new int[]{2494, 5350, 4260, 6750};
        argInts[20] = new int[]{2494, 5350, 4148, 4935};
        argInts[21] = new int[]{2494, 5350, 3305, 6843};
        argInts[22] = new int[]{2494, 5350, 3804, 6701};
        argInts[23] = new int[]{2494, 5350, 4021, 5916};
        argInts[24] = new int[]{2494, 5350, 4498, 6771};
        argInts[25] = new int[]{2494, 5350, 3942, 6449};

        argInts[26] = new int[]{1, 9, 11, 3};

        FisherExactTest fe = new FisherExactTest(19500);

        for (int i = 0; i < argInts.length; i++)
        {
            System.out.println("\nitem " + i);
            System.out.println(
                    "a=" + argInts[i][0] + " b=" + argInts[i][1] + " c=" + argInts[i][2] + " d=" + argInts[i][3]);
            System.out.print("*****Original algorithm: ");
            double cumulativeP = fe.getCumlativeP(argInts[i][0], argInts[i][1], argInts[i][2], argInts[i][3]);
            System.out.println("\tcumulativeP = " + cumulativeP);

            System.out.print("*****Left Tailed: ");
            double leftTailedP = fe.getLeftTailedP(argInts[i][0], argInts[i][1], argInts[i][2], argInts[i][3]);
            System.out.println("\tleftTailedP = " + leftTailedP);

            System.out.print("*****Right Tailed: ");
            double rightTailedP = fe.getRightTailedP(argInts[i][0], argInts[i][1], argInts[i][2], argInts[i][3]);
            System.out.println("\trightTailedP = " + rightTailedP);

            System.out.print("*****Two Tailed: ");
            double twoTailedP = fe.getTwoTailedP(argInts[i][0], argInts[i][1], argInts[i][2], argInts[i][3]);
            System.out.println("\ttwoTailedP = " + twoTailedP);
        }

        System.out.println("-------------------------");
        for (int i = 15; i < argInts.length; i++)
        {
            double p = fe.getP(argInts[i][0], argInts[i][1], argInts[i][2], argInts[i][3]);
//            System.out.println((i - 14) + " : P-value = " + String.format("%.5f", p));
            System.out.println((i - 14) + " : P-value = " + (new DecimalFormat("#.#######").format(p)));
        }
    }
}
