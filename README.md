## MybatisAnnotationTools
基于注解的 Mybatis 代码生成工具，Mybatis-3.5 可用。
### 功能：
1. 自动生成 PO 和 DAO 的 Java 类，DAO 支持分页查询、根据 id 查询、单个插入、批量插入、更新、单个删除、批量删除。  
![Java 文件](https://upload-images.jianshu.io/upload_images/18729964-5b8222f762dad3da.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

UserDao.java 内容如下：
```java
@Mapper
public interface ProjectDao{


    /**
     * 根据id查询
     *
     * @param id
     * @return ProjectDO
     */
    @Select("select  tb_project(id, create_time, modified_time, version, name, status, lines)  from tb_project where id = #{id}")
    @Results({
    	@Result(column = "id",jdbcType= JdbcType.BIGINT,property= "id", id = true),
		@Result(column = "create_time",property="createTime"),
		@Result(column = "modified_time",property="modifiedTime"),
		@Result(column = "version",property="version"),
		@Result(column = "name",property="name"),
		@Result(column = "status",property="status"),
		@Result(column = "lines",property="lines"),
		})
    ProjectDO get(@Param("id")Long  id);

    /**
     * 单个插入
     *
     * @param projectDO ProjectDO
     */
    @Insert("insert into tb_project(id, create_time, modified_time, version, name, status, lines) "
        + "values(#{id}, #{createTime}, #{modifiedTime}, #{version}, #{name}, #{status}, #{lines})")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = Integer.class)
    int insert(ProjectDO projectDO);

    /**
     * 批量插入
     *
     * @param list ProjectDO列表
     */
    @Insert("<script>insert into tb_project(id, create_time, modified_time, version, name, status, lines) values "
        + "<foreach collection='list' index='index' item='n' separator=','> "
        + "(#{n.id}, #{n.createTime}, #{n.modifiedTime}, #{n.version}, #{n.name}, #{n.status}, #{n.lines})"
        + "</foreach></script>")
    void insertBatch(@Param("list") List<ProjectDO> list);

    /**
     * 更新
     *
     * @param projectDO
     */
    @Update("update tb_project set id = #{id}, create_time = #{createTime}, modified_time = #{modifiedTime}, version = #{version}, name = #{name}, status = #{status}, lines = #{lines} where id = #{id}")
    void update(ProjectDO  projectDO);

    /**
     * 单个删除
     *
     * @param id id
     */
    @Delete("delete from tb_project where id = #{id}")
    void delete(Long id);

    /**
     * 批量删除
     *
     * @param ids ids
     */
    @Delete("<script>delete from tb_project where id in "
        + "<foreach collection='ids' index='index' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach></script>")
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 统计
     *
     * @return 数量
     */
    @Select("select count(*) from tb_project")
    int count();
}
```
2. 可配置`application.properties`
```properties
# MySQL 连接配置
mysql.datasource.driver-class-name=com.mysql.jdbc.Driver
mysql.datasource.url=jdbc:mysql://pc-bp1lsgy6vl6yrrl81-test.rwlb.rds.aliyuncs.com:3306/test_1?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false
mysql.datasource.username=qa_msp
mysql.datasource.password=64654dftgert4@
# 要生成的数据库
mysql.datasource.table=
# 表前缀，生成类时会去掉这个前缀
mysql.datasource.table.prefix=tb_
# 是否要生成 PO
java.model.enable=true
# DO 包路径
java.model.package=com.iteknical.wednesday.platform.entity
# DO 类文件生成路径，"/"结尾
java.model.src.folder=src\\main\\java\\com\\luna\\generate\\entity\\
# DO 类文件前缀
java.model.prefix=
# DO 类文件后缀
java.model.suffix=DO
# 是否要生成 DAO
java.dao.enable=true
# DAO 包路径
java.dao.package=com.iteknical.wednesday.platform.dao
# DAO 类文件生成路径，"/"结尾
java.dao.src.folder=src\\main\\java\\com\\luna\\generate\\dao\\
# DAO 类文件前缀
java.dao.prefix=
# DAO 类文件后缀
java.dao.suffix=DAO
```


### 代码结构
 - main 启动类：`Bootstrap.java`
 - 配置文件：`resources/application.properties`目录
 - 模板文件位置：`resources`目录
### 例子
有表`t_student`和`t_user`，用默认配置会在`E:/CODE/github`生成`dao`和`po`目录，里面的内容如下：
```
├─dao
│      BaseDao.java
│      StudentDao.java
│      UserDao.java
│
└─po
        Page.java
        StudentPO.java
        UserPO.java
```
