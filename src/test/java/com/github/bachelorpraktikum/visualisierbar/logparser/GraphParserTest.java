package com.github.bachelorpraktikum.visualisierbar.logparser;

import static org.junit.Assert.assertNotNull;

import com.github.bachelorpraktikum.visualisierbar.model.Context;
import java.io.IOException;
import org.junit.Test;

public class GraphParserTest {

    @Test
    public void testLog1() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog2() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test2.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog3() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test3.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog4() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test4.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog5() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test5.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog6() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test6.zug");
        assertNotNull(context);
    }

    @Test
    public void testLog6WithMSG() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test6MSG.zug");
        assertNotNull(context);
    }

    @Test
    public void testLog7() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test7.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog8() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test8.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog9() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test9.zug.clean");
        assertNotNull(context);
    }

    @Test
    public void testLog10() throws IOException {
        Context context = new GraphParser().parse("src/test/resources/test10.zug.clean");
        assertNotNull(context);
    }
}
