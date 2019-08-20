package com.test;

import com.test.support.util.excel.ExcelReader;
import com.test.support.util.excel.ExcelWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.test.support.util.excel.ExcelWriter.ColumnType.STRING;

/**
 * @author Shoven
 * @date 2019-07-30 15:37
 */
public class BaseTests {

    private long startAt;

    @Before
    public void start() {
        startAt = System.currentTimeMillis();
    }

    @After
    public void end() {
        System.out.println("usage: " + (System.currentTimeMillis() - startAt) + " ms");
        startAt = 0;
    }

    @Test
    public void main() throws IOException {
//        new ExcelReader("F:\\微企宝数据-新\\01.18年12月科目余额表.xls").read((line) -> {
//            if (RandomUtils.nextInt() % 10 == 0) {
//                throw new RuntimeException("金额错误" + line.get("B"));
//            }
//            System.out.println(line.toString());
//        });
//        System.out.println();

    }


}
