package com.alimaddi.Utility;

//import com.sun.jna.*;
//import com.sun.jna.platform.win32.WinDef.*;
//import com.sun.jna.platform.win32.WinNT.HANDLE;

import com.alimaddi.control.downloader.Downloader;
import com.alimaddi.model.STR;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class Utilities
{
//    public static final String ANSI_CSI = "\\x1b[";

//    public static void initConsole()
//    {
//        if(System.getProperty("os.name").startsWith("Windows"))
//        {
//            // Set output mode to handle virtual terminal sequences
//            Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
//            DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
//            HANDLE hOut = (HANDLE)GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});
//
//            DWORDByReference p_dwMode = new DWORDByReference(new DWORD(0));
//            Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
//            GetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, p_dwMode});
//
//            int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
//            DWORD dwMode = p_dwMode.getValue();
//            dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
//            Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
//            SetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, dwMode});
//        }
//    }

    public static void showProgress(long totalSize, long currentProgress, int scale, String moreInfo)
    {
        long total = totalSize / scale + 1;
        long current = currentProgress / total;
        if (currentProgress > totalSize)
            return;
//        System.out.print("\n");
//        System.out.print("\u001B[32m \u001B[Jali6 ali7\n")
//        System.out.print("\033[1A");
//        System.out.print("\u001B[2A");
        System.out.print("\r$ : (");
        for (int i = 0 ; i < current ; i++)
            System.out.print("=");

        if (current < scale)
        {
            System.out.print(">");
            for (long i = ++current ; i < scale ; i++)
                System.out.print(" ");
            System.out.print(")   " + totalSize + "\\" + currentProgress + "   " + moreInfo + "   ");
        }
        if (totalSize == currentProgress)
        {
            System.out.print("\r(");
            for (int i = 0 ; i < scale ; i++)
                System.out.print("=");
            System.out.print(")   " + totalSize + "\\" + currentProgress +"\n$ : done!\n");
        }
    }

    public static String getPromoter(StringBuilder sequence, StringBuilder pattern)
    {
        if (sequence == null || sequence.length() == 0)
            return "";
        if (pattern == null || pattern.length() == 0)
            return "";
        int cdsIndex = (Math.min(pattern.length(), 12));
        int index = sequence.indexOf(pattern.substring(0, cdsIndex));

        return sequence.substring(Math.max(0, Math.max(index, 0) - 120), Math.max(index, 0));
    }

    public static <T> ArrayList<HashSet<T>> split(HashSet<T> original, int splitLength)
    {
        int chunks = original.size() / splitLength;
        if (original.size() % splitLength != 0)
            chunks ++;

        ArrayList<HashSet<T>> result = new ArrayList<HashSet<T>>(chunks);
        Iterator<T> it = original.iterator();

        for (int i = 0; i < chunks; i++)
        {
            HashSet<T> chunk = new HashSet<T>();
            result.add(chunk);
            for (int j = 0; j < splitLength && it.hasNext(); j++)
            {
                chunk.add(it.next());
            }
        }
        return result;
    }


    public static String[] decomposeSTR(String str)
    {
        String[] result = new String[]{"", "0"};
        int firstP;
        int lastP;
        if (str.contains("(") && str.contains(")"))
        {
            firstP = str.indexOf("(");
            lastP = str.indexOf(")");
            if (firstP == 0 && lastP < str.length() - 1)
            {
                result[0] = str.substring(firstP + 1, lastP);
                result[1] = str.substring(lastP + 1);
            }
        }

        return result;
    }

    public static String makeSTRSequence(String str)
    {
        String core;
        int repeat;

        String[] output = Utilities.decomposeSTR(str);
        core = output[0];
        repeat = Integer.parseInt(output[1]);

        return String.join("", Collections.nCopies(repeat, core));
    }

    public static String getDuration(Long startTime, Long endTime)
    {
        Duration duration = Duration.of(endTime - startTime, ChronoUnit.MILLIS);

        return  (duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase());
    }

    public static boolean canBeBioSTR(String core, int repeat)
    {
        return repeat >= 2 &&
                !(core.length() == 1 && repeat < 6) &&
                !(core.length() >= 2 && core.length() < 10 && repeat < 3);
    }

    public static boolean isContainerSTR(STR parent, STR child)
    {
        String extendParent;
        String coreOfParent;
        int repeatOfParent;

        String extendChild;
        String coreOfChild;
        int repeatOfChild;

        String[] output;

        if (parent.equals(child))
            return false;

        output = Utilities.decomposeSTR(parent.getSequence());
        coreOfParent = output[0];
        repeatOfParent = Integer.parseInt(output[1]);

        output = Utilities.decomposeSTR(child.getSequence());
        coreOfChild = output[0];
        repeatOfChild = Integer.parseInt(output[1]);

        extendParent = String.join("", Collections.nCopies(repeatOfParent, coreOfParent));
        extendChild = String.join("", Collections.nCopies(repeatOfChild, coreOfChild));

        return extendParent.equals(extendChild) && coreOfParent.length() < coreOfChild.length();
    }

    public static boolean isHomology(String basePeptide, String candidatePeptide, int method)
    {
        float w1;
        float w2;
        float w3;
        float w4;
        float w5;
        float score = 0;

        if (method == 1)
        {
            w1 = 0.25f;
            w2 = 0.25f;
            w3 = 0.25f;
            w4 = 0.125f;
            w5 = 0.125f;
        }
        else //if (method == 2)
        {
            w1 = 0.2f;
            w2 = 0.2f;
            w3 = 0.2f;
            w4 = 0.2f;
            w5 = 0.2f;
        }

        if (basePeptide.length() > 1 && candidatePeptide.length() > 1 && basePeptide.charAt(1) == candidatePeptide.charAt(1))
            score += w1;
        if (basePeptide.length() > 2 && candidatePeptide.length() > 2 && basePeptide.charAt(2) == candidatePeptide.charAt(2))
            score += w2;
        if (basePeptide.length() > 3 && candidatePeptide.length() > 3 && basePeptide.charAt(3) == candidatePeptide.charAt(3))
            score += w3;
        if (basePeptide.length() > 4 && candidatePeptide.length() > 4 && basePeptide.charAt(4) == candidatePeptide.charAt(4))
            score += w4;
        if (basePeptide.length() > 5 && candidatePeptide.length() > 5 && basePeptide.charAt(5) == candidatePeptide.charAt(5))
            score += w5;
        return score >= 0.5;
    }

    public static String calculateProteinPairwiseScoreBySimple(String referencePeptide, String otherPeptide, int method)
    {
        float w1;
        float w2;
        float w3;
        float w4;
        float w5;
        float score = 0;

        if (method == 1)
        {
            w1 = 0.25f;
            w2 = 0.25f;
            w3 = 0.25f;
            w4 = 0.125f;
            w5 = 0.125f;
        }
        else //if (method == 2)
        {
            w1 = 0.2f;
            w2 = 0.2f;
            w3 = 0.2f;
            w4 = 0.2f;
            w5 = 0.2f;
        }

        if (referencePeptide.length() > 1 && otherPeptide.length() > 1 && referencePeptide.charAt(1) == otherPeptide.charAt(1))
            score += w1;
        if (referencePeptide.length() > 2 && otherPeptide.length() > 2 && referencePeptide.charAt(2) == otherPeptide.charAt(2))
            score += w2;
        if (referencePeptide.length() > 3 && otherPeptide.length() > 3 && referencePeptide.charAt(3) == otherPeptide.charAt(3))
            score += w3;
        if (referencePeptide.length() > 4 && otherPeptide.length() > 4 && referencePeptide.charAt(4) == otherPeptide.charAt(4))
            score += w4;
        if (referencePeptide.length() > 5 && otherPeptide.length() > 5 && referencePeptide.charAt(5) == otherPeptide.charAt(5))
            score += w5;
        return String.format("%.2f", score);
    }

    public static String calculateProteinPairwiseScoreByNeedlemanWunsch(String referencePeptide, String otherPeptide)
    {
        Downloader downloader = new Downloader();
        return downloader.downloadProteinPairwiseEmbossNeedleScore(referencePeptide, otherPeptide);
    }
}
