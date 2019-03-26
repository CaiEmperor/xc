package com.xuecheng.test;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 在cms中使用freemarker将页面生成html文件，本节测试html文件生成的方法:
 * 不与springboot结合,只是用freemarker的api
 */
public class FreemarkerHtml {

    /**
     * 1、使用模板文件静态化
     *      定义模板文件，使用freemarker静态化程序生成html文件。
     */
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //根据版本,创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //或许resources(classpath)路径
        String classpath = this.getClass().getResource("/").getPath();
        //设置模板路径
        configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));
        //设置字符集
        configuration.setDefaultEncoding("utf-8");
        //1.获取模板文件内容
        Template template = configuration.getTemplate("test1.ftl");
        //2.获取数据模型
        Map map = getMap();
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        System.out.println(content);
        //先输入内存,进行版本校验
        InputStream is = IOUtils.toInputStream(content);
        //在输出到文件
        FileOutputStream fos = new FileOutputStream(new File("e:/test1.html"));
        IOUtils.copy(is, fos);
        //关闭资源
        fos.close();
        is.close();
    }

    /**
     * 2、使用模板字符串静态化
     *       定义模板字符串，使用freemarker静态化程序生成html文件
     */
    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException {
        //根据版本,创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //定义模板
        //模板内容(字符串)
        //模板内容，这里测试时使用简单的字符串作为模板
        String templateString="" +
                "<html>\n" +
                "    <head></head>\n" +
                "    <body>\n" +
                "    名称：${name}\n" +
                "    </body>\n" +
                "</html>";
        //加载模板
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        //设置模板名及内容
        stringTemplateLoader.putTemplate("template",templateString);
        //在配置中设置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //1.获取模板内容
        Template template = configuration.getTemplate("template", "utf-8");
        //2.获取数据模型
        Map map = getMap();
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        System.out.println(content);
        //先输入内存,进行版本校验
        InputStream is = IOUtils.toInputStream(content);
        //在输出到文件
        FileOutputStream fos = new FileOutputStream(new File("e:/test1.html"));
        IOUtils.copy(is, fos);
        //关闭资源
        fos.close();
        is.close();
    }

    /**
     * 定义数据模型
     * @return
     */
    private Map getMap(){
        Map<String, Object> map = new HashMap<>();
        //向数据模型放数据
        map.put("name","黑马程序员");
        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMondy(1000.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMondy(200.1f);
        stu2.setAge(19);
//        stu2.setBirthday(new Date());
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        //向数据模型放数据
        map.put("stus",stus);
        //准备map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        //向数据模型放数据
        map.put("stu1",stu1);
        //向数据模型放数据
        map.put("stuMap",stuMap);
        return map;
    }
}
