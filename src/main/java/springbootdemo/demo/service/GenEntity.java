package com.u1city.shop.common;import com.u1city.shop.dto.ShareProfitDto;import java.io.FileWriter;import java.io.IOException;import java.io.PrintWriter;import java.sql.*;import java.text.SimpleDateFormat;import java.util.Date;import java.util.List;//import com.fasterxml.jackson.core.JsonGenerationException;//import com.fasterxml.jackson.databind.JsonMappingException;/** * POJO Product * * @author Tumi 日期：2012-10-10 */public class GenEntity {    private String tableName;// 表名    private String time;// 时间    private String tableComment;// 表注释    private String tableEntity;// 表注释    private String[] colnames; // 列名数组    private String[] colTypes; // 列名类型数组    private String[] colComments; // 列名注释数组    private int priIndex = 0; // 主键列序号    private String[] attNames; // 生成类属性数组    private boolean f_util = false; // 是否需要导入包java.util.*    private boolean f_sql = false; // 是否需要导入包java.sql.*    // 数据库连接    private static final String URL = "jdbc:mysql://192.168.1.141:3306/app_db";    private static final String NAME = "root";    private static final String PASS = "youyicheng";    private static final String DRIVER = "com.mysql.jdbc.Driver";    private static String workSpace = "";    private static String dataBase;    // 文件路径    private String domainPath = workSpace+"U1Shop.Domain/src/main/java/com/u1city/shop/domain/";    private String daoPath = workSpace+"U1Shop.Infrastructure/src/main/java/com/u1city/shop/data/";    private String servicePath = workSpace+"U1Shop.Service/src/main/java/com/u1city/shop/service/";    private String mapperPath = workSpace+"U1Shop.Infrastructure/src/main/java/com/u1city/shop/data/mybatis/";    // 文件包名    private static final String domainPack = "com.u1city.shop.domain";    private static final String daoPack = "com.u1city.shop.data";    private static final String servicePack = "com.u1city.shop.service";    private static final String baseDaoPackage = "com.u1city.shop.baseDao";    private static final String baseServicePackage = "com.c1city.shop.baseService";    private static final String mapperPack = "com.u1city.shop.data.mybatis";    private static String creater = "";    /**     * 出口     *     * @param args     * @throws IOException     * @throws JsonMappingException     * @throws JsonGenerationException     */    public static void main(String[] args) throws Exception {        // creater要替换为你自己的名字缩写后，就不用改那么多处了，直接运行main方法就可以了        creater = "lishangqian";        workSpace = "D:/workspace/";        dataBase = "app_db";		// 指定要映射的表所在的数据库        String table = "tmall_shop_guide_recommend_detail";        GenEntity ge = new GenEntity(table);        ge.genEntity();        ge.genDao();        ge.genService();        ge.genMapper(table);    }    /*     * 构造函数     */    public GenEntity(String table) {        tableName = table;        tableEntity = initcap(formatName(table));        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        time = sdf.format(new Date());        // 创建连接        Connection con;        // sql 语句加入根据库名过滤，避免查到两个相同的表后，导致一个实体类生成翻倍数据 edit by lwf20171227        String sql1 = "SELECT t.TABLE_COMMENT "                + "FROM information_schema.TABLES t " + "WHERE table_name = '"                + tableName + "' and TABLE_SCHEMA='"+dataBase+"'";        String sql2 = "SELECT t.COLUMN_NAME,t.DATA_TYPE,t.COLUMN_COMMENT,t.COLUMN_KEY "                + "FROM information_schema.COLUMNS t "                + "WHERE table_name = '"                + tableName + "' and TABLE_SCHEMA='"+dataBase+"'";        try {            try {                Class.forName(DRIVER);            } catch (ClassNotFoundException e1) {                e1.printStackTrace();            }            con = DriverManager.getConnection(URL, NAME, PASS);            Statement stmt = con.createStatement(); // 创建Statement对象            System.out.println("成功连接到数据库！");            ResultSet rs = stmt.executeQuery(sql1);// 创建数据对象            rs.next();            tableComment = rs.getString(1);            rs = stmt.executeQuery(sql2);// 创建数据对象            rs.last();            int size = rs.getRow();            colnames = new String[size];            colTypes = new String[size];            colComments = new String[size];            attNames = new String[size];            rs.beforeFirst();            int i = 0;            while (rs.next()) {                colnames[i] = rs.getString(1).toLowerCase();                attNames[i] = formatName(colnames[i]);                colTypes[i] = rs.getString(2).toLowerCase();                colComments[i] = rs.getString(3);                if (colTypes[i].equalsIgnoreCase("datetime")) {                    f_util = true;                }                if (colTypes[i].equalsIgnoreCase("image")                        || colTypes[i].equalsIgnoreCase("text")) {                    f_sql = true;                }                if ("pri".equalsIgnoreCase(rs.getString(4))) {                    priIndex = i;                }                i++;            }        } catch (SQLException e) {            e.printStackTrace();        } finally {        }    }    /**     * 功能：生成实体类文件     *     * @param path     */    private void genEntity() {        try {            System.out.println("生成实体类文件begin...");            String content = parse();            FileWriter fw = new FileWriter(domainPath + tableEntity + ".java");            PrintWriter pw = new PrintWriter(fw);            pw.println(content);            pw.flush();            pw.close();            System.out.println("end");        } catch (IOException e) {            e.printStackTrace();        }    }    /**     * 功能：生成实体类主体代码     *     * @param colnames     * @param colTypes     * @param colSizes     * @return     */    private String parse() {        StringBuilder sb = new StringBuilder();        processHead(sb);        // 注释部分        sb.append("/**\r\n");        sb.append(" * " + tableComment + " 实体类\r\n");        sb.append(" * \r\n");        sb.append(" * @author " + time + " "+creater+"\r\n");        sb.append(" */\r\n");        // 实体部分        sb.append("@Entity\r\n");        sb.append("@DynamicInsert\r\n");        sb.append("@DynamicUpdate\r\n");        sb.append("@Table(name = \"" + tableName + "\")\r\n");        sb.append("public class " + tableEntity + " extends DomainObject implements Serializable{\r\n");        sb.append("\r\n");        processAllAttrs(sb);// 属性        sb.append("\r\n");        processAllMethod(sb);// get set方法        sb.append("}");        return sb.toString();    }    /**     * 功能：生成头信息     *     * @param sb     */    private void processHead(StringBuilder sb) {        sb.append("package " + domainPack + ";\r\n");        sb.append("\r\n");        // 判断是否导入工具包        if (f_util) {            sb.append("import java.util.Date;\r\n");            sb.append("\r\n");        }        if (f_sql) {            sb.append("import java.sql.*;\r\n");            sb.append("\r\n");        }        sb.append("import java.io.Serializable;\r\n");        sb.append("import javax.persistence.Column;\r\n");        sb.append("import javax.persistence.Entity;\r\n");        sb.append("import javax.persistence.GeneratedValue;\r\n");        sb.append("import javax.persistence.Id;\r\n");        sb.append("import javax.persistence.Table;\r\n");        sb.append("import javax.persistence.Transient;\r\n");        sb.append("\r\n");    }    /**     * 功能：生成所有属性     *     * @param sb     */    private void processAllAttrs(StringBuilder sb) {        for (int i = 0; i < colnames.length; i++) {            sb.append("\t// " + colComments[i] + "\r\n");            sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " "                    + attNames[i] + ";\r\n");        }    }    /**     * 功能：生成所有方法     *     * @param sb     */    private void processAllMethod(StringBuilder sb) {        sb.append("\t@Transient\r\n");        sb.append("\t@Override\r\n");        sb.append("\tpublic Object getId() {\r\n");        sb.append("\t\treturn this." + attNames[priIndex] + ";\r\n");        sb.append("\t}\r\n");        sb.append("\r\n");        for (int i = 0; i < colnames.length; i++) {            sb.append("\t/**\r\n");            sb.append("\t * Get " + colComments[i] + "\r\n");            sb.append("\t */\r\n");            if (i == priIndex) {                sb.append("\t@Id\r\n");                sb.append("\t@GeneratedValue\r\n");            }            sb.append("\t@Column(name = \"" + colnames[i] + "\")\r\n");            sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get"                    + initcap(attNames[i]) + "() {\r\n");            sb.append("\t\treturn " + attNames[i] + ";\r\n");            sb.append("\t}\r\n");            sb.append("\r\n");            sb.append("\t/**\r\n");            sb.append("\t * Set " + colComments[i] + "\r\n");            sb.append("\t */\r\n");            sb.append("\tpublic void set" + initcap(attNames[i]) + "("                    + sqlType2JavaType(colTypes[i]) + " " + attNames[i]                    + ") {\r\n");            sb.append("\t\tthis." + attNames[i] + " = " + attNames[i] + ";\r\n");            sb.append("\t}\r\n");            sb.append("\r\n");        }    }    /**     * 功能：将输入字符串的首字母改成大写     *     * @param str     * @return     */    private String initcap(String str) {        char[] ch = str.toCharArray();        if (ch[0] >= 'a' && ch[0] <= 'z') {            ch[0] = (char) (ch[0] - 32);        }        return new String(ch);    }    /**     * 功能：获得列的数据类型     *     * @param sqlType     * @return     */    private String sqlType2JavaType(String sqlType) {        if (sqlType.equalsIgnoreCase("bit")) {            return "Boolean";        } else if (sqlType.equalsIgnoreCase("tinyint")) {            return "Integer";        } else if (sqlType.equalsIgnoreCase("smallint")) {            return "Short";        } else if (sqlType.equalsIgnoreCase("int")) {            return "Integer";        } else if (sqlType.equalsIgnoreCase("bigint")) {            return "Long";        } else if (sqlType.equalsIgnoreCase("float")) {            return "Float";        } else if (sqlType.equalsIgnoreCase("double")                || sqlType.equalsIgnoreCase("decimal")                || sqlType.equalsIgnoreCase("numeric")                || sqlType.equalsIgnoreCase("real")                || sqlType.equalsIgnoreCase("money")                || sqlType.equalsIgnoreCase("smallmoney")) {            return "BigDecimal";        } else if (sqlType.equalsIgnoreCase("varchar")                || sqlType.equalsIgnoreCase("char")                || sqlType.equalsIgnoreCase("nvarchar")                || sqlType.equalsIgnoreCase("nchar")                || sqlType.equalsIgnoreCase("text")                || sqlType.equalsIgnoreCase("longtext")) {            return "String";        } else if (sqlType.equalsIgnoreCase("datetime")                || sqlType.equalsIgnoreCase("date")) {            return "Date";        } else if (sqlType.equalsIgnoreCase("image")) {            return "Blod";        }        return null;    }    /**     * 功能：带下划线字符串 驼峰命名     *     * @param str     * @return     */    public String formatName(String str) {        char[] ch = str.toCharArray();        String re = "";        int i = 0;        while (i < ch.length) {            if ('_' == ch[i]) {                if (i + 1 < ch.length) {                    if (ch[i + 1] >= 'a' && ch[i + 1] <= 'z') {                        re += (char) (ch[i + 1] - 32);                        i++;                    }                }            } else {                re += ch[i];            }            i++;        }        return re;    }    /**     * 功能：生成Dao文件     *     */    private void genDao() {        try {            System.out.println("生成Dao文件begin...");            String contentDao = parseDao();            FileWriter fwDao = new FileWriter(daoPath + tableEntity + "Dao.java");            PrintWriter pwDao = new PrintWriter(fwDao);            pwDao.println(contentDao);            pwDao.flush();            pwDao.close();            String contentDaoImpl = parseDaoImpl();            FileWriter fwDaoImpl = new FileWriter(daoPath + "hibernate/impl/"                    + tableEntity + "DaoImpl.java");            PrintWriter pwDaoImpl = new PrintWriter(fwDaoImpl);            pwDaoImpl.println(contentDaoImpl);            pwDaoImpl.flush();            pwDaoImpl.close();            System.out.println("end");        } catch (IOException e) {            e.printStackTrace();        }    }    /**     * 功能：生成Dao主体代码     *     * @return     */    private String parseDao() {        StringBuilder sb = new StringBuilder();        // 头部信息        sb.append("package " + daoPack + ";\r\n");        sb.append("\r\n");        sb.append("import " + domainPack + "." + tableEntity + ";\r\n");        sb.append("import " + baseDaoPackage + ".IBaseDao;\r\n");        sb.append("\r\n");        // 注释部分        sb.append("/**\r\n");        sb.append(" * " + tableComment + " Dao\r\n");        sb.append(" * \r\n");        sb.append(" * @author " + time + " "+creater+"\r\n");        sb.append(" */\r\n");        // 实体部分        sb.append("public interface " + tableEntity + "Dao extends IBaseDao<"                + tableEntity + "> {\r\n");        sb.append("\r\n");        sb.append("}");        return sb.toString();    }    /**     * 功能：生成Dao实现类主体代码     *     * @return     */    private String parseDaoImpl() {        StringBuilder sb = new StringBuilder();        // 头部信息        sb.append("package " + daoPack + ".hibernate.impl;\r\n");        sb.append("\r\n");//		import com.u1city.shop.baseDao.BaseDaoImpl;        sb.append("import "+baseDaoPackage+".BaseDaoImpl;\r\n");        sb.append("\r\n");        sb.append("import " + domainPack + "." + tableEntity + ";\r\n");        sb.append("import " + daoPack + "." + tableEntity + "Dao;\r\n");        sb.append("import org.springframework.stereotype.Repository;\r\n");        sb.append("\r\n");        // 注释部分        sb.append("/**\r\n");        sb.append(" * " + tableComment + " Dao实现类\r\n");        sb.append(" * \r\n");        sb.append(" * @author " + time + " "+creater+"\r\n");        sb.append(" */\r\n");        // 实体部分        sb.append("\r\n@Repository\r\npublic class " + tableEntity                + "DaoImpl extends BaseDaoImpl<" + tableEntity                + ">\r\n");        sb.append("\t\timplements " + tableEntity + "Dao {\r\n");        sb.append("\r\n");//		sb.append("\tpublic " + tableEntity//				+ "DaoImpl(SessionFactory sessionFactory) {\r\n");//		sb.append("\t\tsuper(sessionFactory, " + tableEntity + ".class);\r\n");//		sb.append("\t}\r\n");//		sb.append("\r\n");        sb.append("}");        return sb.toString();    }    /**     * 功能：生成mapper文件     *     */    private void genMapper(String table) {        try {            tableEntity = initcap(formatName(table));            System.out.println("生成Mapper文件begin...");            String contentDao = parseMapper();            FileWriter fwDao = new FileWriter(mapperPath + tableEntity + "Mapper.java");            PrintWriter pwDao = new PrintWriter(fwDao);            pwDao.println(contentDao);            pwDao.flush();            pwDao.close();            String contentDaoImpl = parseMapperImpl();            FileWriter fwDaoImpl = new FileWriter(mapperPath + tableEntity+"Mapper.xml");            PrintWriter pwDaoImpl = new PrintWriter(fwDaoImpl);            pwDaoImpl.println(contentDaoImpl);            pwDaoImpl.flush();            pwDaoImpl.close();            System.out.println("end");        } catch (IOException e) {            e.printStackTrace();        }    }    /**     * 功能：生成Dao主体代码     *     * @return     */    private String parseMapper() {        StringBuilder sb = new StringBuilder();        // 头部信息        sb.append("package " + mapperPack + ";\r\n");        sb.append("\r\n");//        sb.append("import " + domainPack + "." + tableEntity + ";\r\n");//        sb.append("import " + baseDaoPackage + ".IBaseDao;\r\n");        sb.append("\r\n");        // 注释部分        sb.append("/**\r\n");        sb.append(" * " + tableComment + " mapper\r\n");        sb.append(" * \r\n");        sb.append(" * @author " + time + " "+creater+"\r\n");        sb.append(" */\r\n");        // 实体部分        sb.append("public interface " + tableEntity + "Mapper "  + " {\r\n");        sb.append("\r\n");        sb.append("}");        return sb.toString();    }    /**     * 功能：生成Dao实现类主体代码     *     * @return     */    private String parseMapperImpl() {        StringBuilder sb = new StringBuilder();        // 头部信息        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");        sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >");        sb.append("\r\n");        sb.append("<mapper namespace=\""+ mapperPack+"."+tableEntity+"Mapper\">");        sb.append("\r\n");        sb.append("\r\n");        sb.append("</mapper>");        return sb.toString();    }    /**     * 功能：生成Service文件     *     * @param path     */    private void genService() {        try {            System.out.println("生成Service文件begin...");            String contentDao = parseService();            FileWriter fwDao = new FileWriter(servicePath + tableEntity                    + "Service.java");            PrintWriter pwDao = new PrintWriter(fwDao);            pwDao.println(contentDao);            pwDao.flush();            pwDao.close();            String contentDaoImpl = parseServiceImpl();            FileWriter fwDaoImpl = new FileWriter(servicePath + "impl/" + tableEntity                    + "ServiceImpl.java");            PrintWriter pwDaoImpl = new PrintWriter(fwDaoImpl);            pwDaoImpl.println(contentDaoImpl);            pwDaoImpl.flush();            pwDaoImpl.close();            System.out.println("end");        } catch (IOException e) {            e.printStackTrace();        }    }    /**     * 功能：生成Service主体代码     *     * @return     */    private String parseService() {        StringBuilder sb = new StringBuilder();        // 头部信息        sb.append("package " + servicePack + ";\r\n");        sb.append("\r\n");        sb.append("import " + domainPack + "." + tableEntity + ";\r\n");        sb.append("\r\n");        sb.append("import " + baseServicePackage + ".ServiceSupport;\r\n");        sb.append("\r\n");        // 注释部分        sb.append("/**\r\n");        sb.append(" * " + tableComment + " Service\r\n");        sb.append(" * \r\n");        sb.append(" * @author " + time + " "+creater+"\r\n");        sb.append(" */\r\n");        // 实体部分        sb.append("public interface " + tableEntity                + "Service extends ServiceSupport<" + tableEntity + "> {\r\n");        sb.append("\r\n");        sb.append("}");        return sb.toString();    }    /**     * 功能：生成Service实现类主体代码     *     * @return     */    private String parseServiceImpl() {        StringBuilder sb = new StringBuilder();        // 头部信息        sb.append("package " + servicePack + ".impl;\r\n");        sb.append("\r\n");        sb.append("import " + domainPack + "." + tableEntity + ";\r\n");        sb.append("import " + daoPack + "." + tableEntity + "Dao;\r\n");        sb.append("import " + baseServicePackage + ".ServiceSupportImpl;\r\n");        sb.append("import org.springframework.stereotype.Service;\r\n");        sb.append("import org.springframework.beans.factory.annotation.Autowired;\r\n");        sb.append("import " + servicePack + "." + tableEntity + "Service;\r\n");        sb.append("\r\n");        // 注释部分        sb.append("/**\r\n");        sb.append(" * " + tableComment + " Service实现类\r\n");        sb.append(" * \r\n");        sb.append(" * @author " + time + " "+creater+"\r\n");        sb.append(" */\r\n");        // 实体部分        sb.append("\r\n@Service\r\npublic class " + tableEntity                + "ServiceImpl extends ServiceSupportImpl<" + tableEntity                + "> implements\r\n");        sb.append("\t\t" + tableEntity + "Service {\r\n");        sb.append("\r\n");        String lowerCase = tableEntity.substring(0,1).toLowerCase();        String substring = tableEntity.substring(1);        String lowerCaseEntity = lowerCase + substring;        String daoParam = lowerCaseEntity + "Dao";        sb.append("\tprivate " + tableEntity + "Dao "+daoParam+";\r\n");        sb.append("\r\n");        sb.append("\r\n\t@Autowired\r\n\tpublic void set" + tableEntity + "Dao(" + tableEntity                + "Dao "+daoParam+") {\r\n");        sb.append("\t\tsuper.dao = "+daoParam+";\r\n");        sb.append("\t\tthis."+daoParam+" = "+daoParam+";\r\n");        sb.append("\t}\r\n");        sb.append("\r\n");        sb.append("}");        return sb.toString();    }    public List<ShareProfitDto> addProfit(List<ShareProfitDto> profits,                                          ShareProfitDto profit) {        for (int i = 0; i < profits.size(); i++) {            if (profits.get(i).getItemId().equals(profit.getItemId())                    && profits.get(i).getSkuId().equals(profit.getSkuId())                    && profits.get(i).getDistributorNick()                    .equals(profit.getDistributorNick())                    && profits.get(i).getBrandingBusinessNick()                    .equals(profit.getBrandingBusinessNick())) {                ShareProfitDto dto = profits.get(i);                dto.setDistributionRate(dto.getDistributionRate()                        + profit.getDistributionRate());                profits.set(i, dto);                return profits;            }        }        profits.add(profit);        return profits;    }}