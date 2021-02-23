package com.luna.generate;

import com.google.common.base.CaseFormat;
import com.luna.common.os.OSinfo;
import com.luna.generate.creator.DaoCreator;
import com.luna.generate.creator.ModelCreator;
import com.luna.generate.model.Field;
import com.luna.generate.model.Table;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * @author luna
 * @since 2019/7/29 11:01
 */
public class Bootstrap {

    private static final String     ENABLED        = "true";
    private static final String     PROP_FILE_NAME = "application.properties";

    private static String           propPath       = "";
    private static final Properties PROP           = new Properties();

    public static void main(String[] args) throws Exception {

        propPath = StringUtils.isAllBlank(args) ? "" : args[0];

        Map<String, List<Field>> tableFields = new HashMap<>();

        // 读取配置文件
        Table table = queryProperties(tableFields);

        // 获取数据库信息
        queryDatabase(tableFields);

        // 创建 Java 文件
        createJavaFiles(table);

        System.out.println("Finished, find " + tableFields.size() + " tables");
    }

    private static Table queryProperties(Map<String, List<Field>> tableFields) throws IOException {
        if (StringUtils.isBlank(propPath)) {
            try (
                InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
                PROP.load(resourceAsStream);
            }
        } else {
            try (InputStream resourceAsStream = new FileInputStream(new File(propPath))) {
                PROP.load(resourceAsStream);
            }
        }

        Table table = new Table();

        if (OSinfo.isWindows()) {
            table.setModelFolder(PROP.getProperty("win.java.model.src.folder"));
            table.setDaoFolder(PROP.getProperty("win.java.dao.src.folder"));
        } else if (OSinfo.isMacOSX() || OSinfo.isMacOS()) {
            table.setModelFolder(PROP.getProperty("mac.java.model.src.folder"));
            table.setDaoFolder(PROP.getProperty("mac.java.dao.src.folder"));
        }
        table.setModelPackage(PROP.getProperty("java.model.package"));
        table.setDaoPackage(PROP.getProperty("java.dao.package"));
        table.setModelPrefix(PROP.getProperty("java.model.prefix"));
        table.setModelSuffix(PROP.getProperty("java.model.suffix"));
        table.setDaoPrefix(PROP.getProperty("java.dao.prefix"));
        table.setDaoSuffix(PROP.getProperty("java.dao.suffix"));
        table.setTablePrefix(PROP.getProperty("mysql.datasource.table.prefix"));
        table.setTableFields(tableFields);
        return table;
    }

    private static String getJavaFieldName(String columnName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);
    }

    private static void createJavaFiles(Table table) {
        final String modelEnable = PROP.getProperty("java.model.enable");
        if (ENABLED.equals(modelEnable)) {
            DaoCreator daoCreator = new DaoCreator(table);
            daoCreator.createDao();
        }
        final String daoEnable = PROP.getProperty("java.dao.enable");
        if (ENABLED.equals(daoEnable)) {
            ModelCreator modelCreator = new ModelCreator(table);
            modelCreator.createModel();
        }

    }

    private static void queryDatabase(Map<String, List<Field>> tableFields)
        throws ClassNotFoundException, SQLException {
        Class.forName(PROP.getProperty("mysql.datasource.driver-class-name"));
        String url = PROP.getProperty("mysql.datasource.url");
        String username = PROP.getProperty("mysql.datasource.username");
        String password = PROP.getProperty("mysql.datasource.password");
        String table = PROP.getProperty("mysql.datasource.table");
        try (Connection conn = DriverManager.getConnection(url, username, password)) {

            final DatabaseMetaData metaData;
            metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] {"TABLE"});

            while (tables.next()) {
                // mysql中这里改成3
                final String tableName = tables.getString(3);
                // 这里优化了一下 导出自己需要的表就行 没必要全部导出
                if (!(table).equals(tableName)) {
                    continue;
                }
                System.out.println(tableName + "-------------");
                final String tableRemarks = tables.getString("REMARKS");
                ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);
                String primaryKey = null;
                if (primaryKeys.next()) {
                    primaryKey = primaryKeys.getString("COLUMN_NAME");
                }

                ResultSet rs = metaData.getColumns(null, null, tableName, "%");
                List<Field> fields = new ArrayList<>();
                while (rs.next()) {
                    final String columnName = rs.getString("COLUMN_NAME");
                    final String typeName = rs.getString("TYPE_NAME");
                    final int columnSize = rs.getInt("COLUMN_SIZE");
                    final int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                    final String remarks = rs.getString("REMARKS");
                    final String nullable = rs.getString("IS_NULLABLE");
                    Field field = new Field();
                    field.setFieldName(columnName);
                    field.setFieldType(typeName);
                    field.setJavaFieldName(getJavaFieldName(columnName));
                    field.setRemarks(remarks);
                    field.setKey(columnName.equals(primaryKey));
                    field.setPrecision(columnSize);
                    field.setScale(decimalDigits);
                    field.setNullable("NO".equalsIgnoreCase(nullable));
                    fields.add(field);
                }
                tableFields.put(tableName.concat("#").concat(tableRemarks), fields);
            }
        }
    }

}
