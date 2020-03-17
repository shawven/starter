package com.starter.demo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.starter.demo.support.util.NodeTree;
import com.starter.demo.support.util.excel.ExcelReader;
import com.starter.demo.support.util.excel.ExcelWriter;
import com.sun.xml.internal.ws.encoding.RootOnlyCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.assertj.core.util.Lists;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocumentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.security.Security;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
    public void test() {
        List<Boolean> booleans = Stream.of(true, false, true, false)
                .sorted(Comparator.comparing(Boolean::booleanValue).reversed())
                .collect(toList());
        System.out.println(booleans);
    }


    @Test
    public void testCopyProperties() {
        class A {
            private String aa;
            private String bb;

            public String getAa() {
                return aa;
            }

            public void setAa(String aa) {
                this.aa = aa;
            }

            public String getBb() {
                return bb;
            }

            public void setBb(String bb) {
                this.bb = bb;
            }

            public A(String aa, String bb) {
                this.aa = aa;
                this.bb = bb;
            }

            @Override
            public String toString() {
                return "A{" +
                        "aa='" + aa + '\'' +
                        ", bb='" + bb + '\'' +
                        '}';
            }
        }
        A a = new A("A", "1");
        A b = new A("B", null);

        BeanUtils.copyProperties(a, b);

        A a1 = new A("A", "1");
        A b1 = new A("B", null);
        BeanUtils.copyProperties(b1, a1);
        System.out.println();
    }

    @Test
    public void testLocalDateTime() throws Exception {

        LocalDateTime startTime = YearMonth.parse("2019-10")
                // 第一天
                .atDay(1).atStartOfDay();
        LocalDateTime endTime = YearMonth.parse("2019-10")
                // 加一个月
                .plusMonths(1)
                // 第一天
                .atDay(1).atStartOfDay()
                // 减一毫秒  （减一秒 ChronoUnit.SECONDS）
                .minus(1, ChronoUnit.MILLIS);


        System.out.println("日初：" + startTime);
        System.out.println("日末：" + endTime);
        System.out.println("日初秒时间戳：" + startTime.toEpochSecond(ZoneOffset.ofHours(8)));
        System.out.println("日末秒时间戳：" + endTime.toEpochSecond(ZoneOffset.ofHours(8)));
        System.out.println("日末毫秒时间戳：" + endTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
    }

    @Test
    public  void testXml() throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File("E:/转换工具/树立水.xml"));

        FileWriter fileWriter = new FileWriter("E:/转换工具/树立水_美化.xml");
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);
        XMLWriter writer = new XMLWriter(fileWriter, format);
        writer.write(document);
        writer.close();
    }


    @Test
    public void testRangeMap() {
        RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
        rangeMap.put(Range.greaterThan(1), "1");
        rangeMap.put(Range.greaterThan(2), "2");
        rangeMap.put(Range.greaterThan(3), "3");
        rangeMap.put(Range.greaterThan(4), "4");
        rangeMap.put(Range.greaterThan(5), "5");

        rangeMap.get(3);
    }

    @Test
    public void testNodeTree2() {
        class Menu implements NodeTree.Node<Menu> {

            @Override
            public List<Menu> getChildren() {
                return null;
            }

            @Override
            public void setChildren(List<Menu> children) {

            }
        }

    }
    @Test
    public void testNodeTree() {
        class Menu implements NodeTree.Node<Menu> {
            String name;
            String id;

            private List<Menu> children;

            public Menu(String name, String id) {
                this.name = name;
                this.id = id;
            }

            @Override
            public List<Menu> getChildren() {
                return children;
            }

            @Override
            public void setChildren(List<Menu> children) {
                this.children = children;
            }
        }


        ArrayList<Menu> menus = Lists.newArrayList(
                new Menu("A", "1"),
                new Menu("AA", "11"),
                new Menu("AAA", "111"),
                new Menu("B", "2"));

        List<Menu> tree = NodeTree.<Menu, Menu>from(menus)
                .rootFilter(menu -> menu.id.length() == 1)
                .childFilter((parent, child) -> parent.id.concat("1").equals(child.id))
                .build();

        System.out.println(tree);
    }

    @Test
    public void testExcelReader() throws Exception {
        ExcelReader excelReader = new ExcelReader("d:/03.xls");
        excelReader = new ExcelReader("d:/07.xlsx");
        excelReader.stream();
    }

    @Test
    public void testExcelWriter() throws Exception {
        class A {
            private String aa;
            private String bb;
            private Map<String, Object> children;

            {
                children = new HashMap<>();
                Map<String, String> hobby = new HashMap<>();
                hobby.put("name", "pig");
                hobby.put("cost", "100$");
                children.put("firstName", "hello");
                children.put("lastName", "word");
                children.put("hobby", hobby);
            }

            public String getAa() {
                return aa;
            }

            public String getBb() {
                return bb;
            }

            public A(String aa, String bb) {
                this.aa = aa;
                this.bb = bb;
            }
        }
        ImmutableList<A> items = ImmutableList.of(
                new A("aaaa11", "bbbbb11"),
                new A("aaaa22", "bbbbb22"),
                new A("aaaa33", "bbbbb33")
        );

        ExcelWriter.Column complexColumn = new ExcelWriter.Column();
        complexColumn.setTitle("父单元格");
        complexColumn.setKey("children");
        complexColumn.setChildColumns(Lists.newArrayList(
                new ExcelWriter.Column().setTitle("firstName").setKey("firstName"),
                new ExcelWriter.Column().setTitle("lastName").setKey("lastName"),
                new ExcelWriter.Column().setTitle("hobby").setKey("hobby")
                        .setChildColumns(Lists.newArrayList(
                                new ExcelWriter.Column().setTitle("name").setKey("name"),
                                new ExcelWriter.Column().setTitle("cost").setKey("cost")
                                )
                        )
        ));

        ExcelWriter excelWriter = new ExcelWriter()
                .setData(items)
                .setTitle("wes111")
                .addColumn("AA", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn("BB", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn(complexColumn)
                .save();

        excelWriter.createSheet()
                .setData(items)
                .setTitle("wes22")
                .addColumn("AA", "aa", ExcelWriter.ColumnType.STRING)
                .addColumn("BB", "bb", ExcelWriter.ColumnType.STRING)
                .save();

        excelWriter.writeToFile("d:/test.xlsx");
    }

    @Test
    public void testLockedExcelWriter() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("sheet名称");

        XSSFCellStyle lockstyle = wb.createCellStyle();
        lockstyle.setLocked(true);//设置锁定

        lockstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        byte[] rgb = {(byte) 240, (byte) 240, (byte) 240};
        lockstyle.setFillForegroundColor(new XSSFColor(rgb, new DefaultIndexedColorMap()));

        lockstyle.setBorderTop(BorderStyle.THIN);
        lockstyle.setBorderLeft(BorderStyle.THIN);
        lockstyle.setBorderRight(BorderStyle.THIN);
        lockstyle.setBorderBottom(BorderStyle.THIN);
        lockstyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockstyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockstyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockstyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

        XSSFCellStyle unlockStyle = wb.createCellStyle();
        unlockStyle.setLocked(false);//设置未锁定


        for (int i = 0; i < 10; i++) {
            XSSFRow row = sheet.createRow(i);
            for (int j = 0; j < 10; j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellStyle(unlockStyle);//默认是锁定状态；将所有单元格设置为：未锁定；然后再对需要上锁的单元格单独锁定
                if (j == 1) {//这里可以根据需要进行判断;我这就将第2列上锁了
                    cell.setCellStyle(lockstyle);//将需要上锁的单元格进行锁定
                    cell.setCellValue("上锁了");
                } else {
                    cell.setCellValue("没上锁了");
                }
            }
        }
        //sheet添加保护，这个一定要否则光锁定还是可以编辑的
        sheet.protectSheet("123456");
        FileOutputStream os = new FileOutputStream("D:\\" + System.currentTimeMillis() + ".xlsx");
        wb.write(os);
        os.close();
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }


    private Class class1() {
        return ApplicationTests.class;
    }

    private Class<?> class2() {
        return ApplicationTests.class;
    }

    private Class<ApplicationTests> class3() {
        return ApplicationTests.class;
    }


}
