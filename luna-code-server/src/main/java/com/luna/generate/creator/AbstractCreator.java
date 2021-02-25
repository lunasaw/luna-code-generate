package com.luna.generate.creator;

import java.util.List;
import java.util.function.BiConsumer;

import com.luna.generate.model.Field;
import com.luna.generate.model.Table;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

/**
 * @author luna
 * @since 2019/7/29 19:12
 */

abstract class AbstractCreator {

    /** mac */
    static final String         MAC_MODEL_TEMP = "luna-code-server/src/main/resources/EntityTemplate";
    static final String         MAC_DAO_TEMP   = "luna-code-server/src/main/resources/DaoTemplate";
    /** windows */
    static final String         WIN_MODEL_TEMP = "src\\main\\resources\\EntityTemplate";
    static final String         WIN_DAO_TEMP   = "src\\main\\resources\\DaoTemplate";

    static final String         SPLIT_CHAR     = "#";
    private static final String JAVA           = ".java";

    Table                       table;

    public AbstractCreator(Table table) {
        this.table = table;
    }

    void fieldsForEach(BiConsumer<String, List<Field>> fields) {
        table.getTableFields().forEach(fields);
    }

    String getDaoFilePath(String tableName) {
        return table.getDaoFolder().trim().concat(getDaoName(tableName).concat(JAVA));
    }

    String getDaoName(String tableName) {
        return table.getDaoPrefix()
            .concat(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                tableName.split(SPLIT_CHAR)[0].replaceAll(table.getTablePrefix(), StringUtils.EMPTY)))
            .concat(table.getDaoSuffix()).trim();
    }

    String getModelFilePath(String tableName) {
        return table.getModelFolder().trim().concat(getModelName(tableName).concat(JAVA));
    }

    String getModelName(String tableName) {
        return table.getModelPrefix()
            .concat(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                tableName.split(SPLIT_CHAR)[0].replaceAll(table.getTablePrefix(), StringUtils.EMPTY)))
            .concat(table.getModelSuffix()).trim();
    }
}
