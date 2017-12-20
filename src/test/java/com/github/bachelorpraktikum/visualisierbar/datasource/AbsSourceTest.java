package com.github.bachelorpraktikum.visualisierbar.datasource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class AbsSourceTest {

    // possible products: Small, SmallFix, SmallDrive, SmallBreak, SmallBreakFix, ETCS1, ETCS2
    // but some of them are not able to compile
    private ArrayList<String> products;
    private ArrayList<String[]> expected;

    @Before
    public void init()
        {products = new ArrayList<>(Arrays.asList("Small",
                                                  "SmallBreak",
                                                  "ETCS1"));
         expected = new ArrayList<>(Arrays.asList(new String[]{"SmallEx"},
                    new String[]{"SmallEx", "RandBreak"},
                    new String[]{"ETCS1Ex", "ETCSRBC", "ETCSTimer", "ETCSUtil", "Balises", "ETCSCore"}));

        }


    @Test
    public void product0() throws IOException
        {String command = String.format("absc -v -product=%s -erlang src/test/resources/ABS/Main/*.abs -d src/test/resources/ABS/gen/erlang/",
                products.get(0));
         File path = new File("src/test/resources/ABS/Main/");
         AbsSource source = new AbsSource(command, path, products.get(0));
         ArrayList foundedDeltas = source.getDeltas();
         System.out.println(foundedDeltas);

         assertArrayEquals(expected.get(0), foundedDeltas.toArray());
        }

    @Test
    public void product1() throws IOException
        {String command = String.format("absc -v -product=%s -erlang src/test/resources/ABS/Main/*.abs -d src/test/resources/ABS/gen/erlang/",
            products.get(1));
         File path = new File("src/test/resources/ABS/Main/");
         AbsSource source = new AbsSource(command, path, products.get(1));
         ArrayList foundedDeltas = source.getDeltas();
         System.out.println(foundedDeltas);

         assertArrayEquals(expected.get(1), foundedDeltas.toArray());
        }

    @Test
    public void product2() throws IOException
        {String command = String.format("absc -v -product=%s -erlang src/test/resources/ABS/Main/*.abs -d src/test/resources/ABS/gen/erlang/",
                products.get(2));
         File path = new File("src/test/resources/ABS/Main/");
         AbsSource source = new AbsSource(command, path, products.get(2));
         ArrayList foundedDeltas = source.getDeltas();

         assertArrayEquals(expected.get(2), foundedDeltas.toArray());
        }


    @After
    public void deleteOutputFile() {
        File delete = new File("src/test/resources/ABS/actual.zug");
        delete.delete();
    }
}
