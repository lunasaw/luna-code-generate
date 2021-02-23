package com.luna.generate.creator;

import com.luna.common.os.OSinfo;
import com.luna.common.utils.FileUtils;
import com.luna.generate.model.Field;
import com.luna.generate.model.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author luna
 * @since 2019/7/29 11:01
 */

public class DaoCreator extends AbstractCreator {

    public DaoCreator(Table table) {
        super(table);
    }

    public void createDao() {
        fieldsForEach((longTableName, fields) -> {
            FileUtils.writeStringToFile(
                getDaoFilePath(longTableName), createDaoFileContent(longTableName));
        });
    }

    private String createDaoFileContent(String longTableName) {
        final String[] tableNames = longTableName.split(SPLIT_CHAR);
        final String tableName = tableNames[0];
        String fileContent = "";
        if (OSinfo.isWindows()) {
            fileContent = FileUtils.readFileToString(WIN_DAO_TEMP);
        } else if (OSinfo.isMacOSX() || OSinfo.isMacOS()) {
            fileContent = FileUtils.readFileToString(MAC_DAO_TEMP);
        }
        final String tableComment = tableNames.length > 1 ? tableNames[1] : "";
        fileContent = fileContent.replaceAll("#<tableComment>", tableComment);
        fileContent = fileContent.replaceAll("#<tableName>", tableName);

        fileContent = fileContent.replaceAll("#<fields>", getFields(longTableName));
        fileContent = fileContent.replaceAll("#<insertValues>", getInsertValues(longTableName));
        fileContent = fileContent.replaceAll("#<batchInsertValues>", getBatchInsertValues(longTableName));
        fileContent = fileContent.replaceAll("#<updateValues>", getUpdateValues(longTableName));
        fileContent = fileContent.replaceAll("#<daoPackage>", table.getDaoPackage());
        System.out.println(getDaoName(tableName));
        fileContent = fileContent.replaceAll("#<daoName>", getDaoName(tableName));
        // model
        fileContent = fileContent.replaceAll("#<modelPackage>", table.getModelPackage());
        fileContent = fileContent.replaceAll("#<modelName>", getModelName(tableName));
        fileContent = fileContent.replaceAll("#<modelNameLower>",
            getModelName(tableName).substring(0, 1).toLowerCase() + getModelName(tableName).substring(1));
        fileContent = fileContent.replaceAll("#<createUser>", System.getProperty("user.name"));
        final String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        fileContent = fileContent.replaceAll("#<createTime>", createTime);
        // 这里做了优化 @Results获取字段对应的数据
        fileContent = fileContent.replaceAll("#<result>", getResults(longTableName));
        return fileContent;
    }

    private String getUpdateValues(String tableName) {
        List<Field> fields = table.getTableFields().get(tableName);
        StringBuilder builder = new StringBuilder();
        Field field = fields.get(0);
        String rowContent =
            field.getFieldName() + " = #{#<property>}".replaceAll("#<property>", field.getJavaFieldName());
        builder.append(rowContent);

        for (int i = 1; i < fields.size(); i++) {
            field = fields.get(i);
            rowContent = field.getFieldName() + " = #{#<property>}".replaceAll("#<property>", field.getJavaFieldName());
            builder.append(", ").append(rowContent);
        }
        return builder.toString();
    }

    private String getBatchInsertValues(String tableName) {
        List<Field> fields = table.getTableFields().get(tableName);
        StringBuilder builder = new StringBuilder();
        Field field = fields.get(0);
        String rowContent = "#{#<property>}".replaceAll("#<property>", "n." + field.getJavaFieldName());
        builder.append(rowContent);

        for (int i = 1; i < fields.size(); i++) {
            field = fields.get(i);
            rowContent = "#{#<property>}".replaceAll("#<property>", "n." + field.getJavaFieldName());
            builder.append(", ").append(rowContent);
        }
        return builder.toString();
    }

    private String getInsertValues(String tableName) {
        List<Field> fields = table.getTableFields().get(tableName);
        StringBuilder builder = new StringBuilder();
        Field field = fields.get(0);
        String rowContent = "#{#<property>}".replaceAll("#<property>", field.getJavaFieldName());
        builder.append(rowContent);

        for (int i = 1; i < fields.size(); i++) {
            field = fields.get(i);
            rowContent = "#{#<property>}".replaceAll("#<property>", field.getJavaFieldName());
            builder.append(", ").append(rowContent);
        }
        return builder.toString();
    }

    private String getFields(String tableName) {
        List<Field> fields = table.getTableFields().get(tableName);
        StringBuilder builder = new StringBuilder();
        builder.append(fields.get(0).getFieldName());
        for (int i = 1; i < fields.size(); i++) {
            Field field = fields.get(i);
            builder.append(", ").append(field.getFieldName());
        }
        return builder.toString();
    }

    private String getResults(String tableName) {
        List<Field> fields = table.getTableFields().get(tableName);
        StringBuilder builder = new StringBuilder();
        Field field0 = fields.get(0);
        builder.append("\t@Result(column = \"" + field0.getFieldName() + "\",property= \""
            + field0.getJavaFieldName() + "\", id = true),\r\n");

        for (int i = 1; i < fields.size(); i++) {
            Field field = fields.get(i);
            builder.append("\t\t@Result(column = \"" + field.getFieldName() + "\",property=\""
                + field.getJavaFieldName() + "\"),\r\n");
        }
        builder.append("\t\t");
        return builder.toString();
    }
}
