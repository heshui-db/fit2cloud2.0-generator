package com.fit2cloud.generator;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;

import java.io.*;
import java.util.Properties;

public class CodeGenerator {

    private static final String DEFAULT_APPLICATION_NAME = "fit2cloud2.0-demo";
    private static final String DEFAULT_PROJECT_NAME = "Demo";
    private static final String DEFAULT_GROUP_ID = "com.fit2cloud";
    private static final String DEFAULT_VERSION = "2.0.0";
    private static final String TEMPLATE_DIR = "template";
    private static GroupTemplate gt;

    static {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(
                TEMPLATE_DIR);
        Configuration cfg;
        try {
            cfg = Configuration.defaultConfiguration();
            cfg.setPlaceholderStart("#{");
            cfg.setPlaceholderEnd("}");
            gt = new GroupTemplate(resourceLoader, cfg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String applicationName;
    //项目名称
    private String projectName;
    //项目描述
    private String projectSummary;
    //项目顺序
    private Integer projectOrder = 20;
    //项目端口
    private Integer projectPort = 8080;
    //生成项目本机路径
    private String projectPath;
    //项目包路径
    private String packagePath;
    private String groupId;
    private String version;
    private String TEMPLATE_RESOURCE = this.getClass().getClassLoader().getResource(TEMPLATE_DIR).getPath();

    CodeGenerator() {
        banner();
        Properties properties = readConfig();
        this.applicationName = properties.getProperty("application.name", DEFAULT_APPLICATION_NAME);
        this.projectName = properties.getProperty("module.name", DEFAULT_PROJECT_NAME);
        this.projectSummary = properties.getProperty("module.summary", DEFAULT_PROJECT_NAME);
        try {
            this.projectOrder = Integer.parseInt(properties.getProperty("module.order"));
        } catch (Exception e) {
            //not do
        }
        try {
            this.projectPort = Integer.parseInt(properties.getProperty("module.port"));
        } catch (Exception e) {
            //not do
        }
        this.projectPath = properties.getProperty("projectPath",
                System.getProperty("user.home"));
        this.packagePath = properties.getProperty("package", "com.fit2cloud." + capturePackageName(applicationName));

        this.groupId = properties.getProperty("groupId", DEFAULT_GROUP_ID);

        this.version = properties.getProperty("version", DEFAULT_VERSION);

        System.out.println("-----------初始化配置信息-----------");
    }

    void build() {
        System.out.println("-----------开始生成项目-----------");
        try {
            createProject();
        } catch (Exception e) {
            throw new RuntimeException("code build error", e);
        }
        handleProject(this.projectPath + File.separator + this.applicationName);
        System.out.println("-----------项目生成成功-----------");
        System.out.println("-----------项目路径:" + this.projectPath + File.separator + this.applicationName);
        treeProject();
    }

    private File createProject() throws Exception {
        File rootDir = new File(this.projectPath + File.separator + this.applicationName);
        rootDir.mkdir();
        generate(TEMPLATE_RESOURCE);
        return rootDir;
    }

    private void generate(String templatePath) throws Exception {
        File file = new File(templatePath);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    if (f.getName().equals("com")) {
                        this.generateDirectory(capturePackage(f.getAbsolutePath().replace(TEMPLATE_RESOURCE, "")));
                        this.generate(templatePath + "/com/fit2cloud/demo");
                    } else {
                        this.generateDirectory(capturePath(f.getAbsolutePath().replace(TEMPLATE_RESOURCE, "")));
                        this.generate(templatePath + "/" + f.getName());
                    }
                } else {
                    this.generateFile(f.getAbsolutePath().replace(TEMPLATE_RESOURCE, ""));
                }
            }
        }
    }

    private String capturePackage(String path) {
        return path.substring(0, path.length() - 3) + this.packagePath.replace(".", "/");
    }

    private String capturePath(String path) {
        return path.replace("com/fit2cloud/demo", this.packagePath.replace(".", "/"));
    }

    private void generateDirectory(String templateDir) {
        File targetFile = new File(this.projectPath + File.separator + applicationName + templateDir);
        targetFile.mkdirs();
    }

    private void generateFile(String templateDir) throws Exception {
        File targetFile = new File(this.projectPath + File.separator + applicationName + capturePath(templateDir));
        OutputStream os = new FileOutputStream(targetFile);
        Template t = gt.getTemplate(templateDir);
        t.binding("applicationName", this.applicationName.trim());
        t.binding("projectName", string2Unicode(this.projectName.trim()));
        t.binding("projectSummary", string2Unicode(this.projectSummary.trim()));
        t.binding("projectOrder", this.projectOrder);
        t.binding("projectPort", this.projectPort);
        t.binding("packagePath", this.packagePath.trim());
        t.binding("groupId", this.groupId.trim());
        t.binding("version", this.version.trim());
        t.renderTo(os);
        os.close();
    }

    private String capturePackageName(String name) {
        if (name == null || name.equals("")) throw new RuntimeException("project name is empty");
        if (name.contains("-")) {
            String[] strings = name.split("-");
            name = strings[strings.length - 1];
        }
        return name.toLowerCase();
    }

    private Properties readConfig() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("read config file error", e);
        }
        return properties;
    }

    private String string2Unicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch < 0x10) {
                unicode.append("\\u000");
            } else if (ch < 0x100) {
                unicode.append("\\u00");
            } else if (ch < 0x1000) {
                unicode.append("\\u0");
            } else {
                unicode.append("\\u");
            }
            unicode.append(Integer.toHexString(ch));
        }
        return unicode.toString();
    }

    private void handleProject(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    handleProject(f.getAbsolutePath());
                } else {
                    if (".gitkeep".equals(f.getName())) {
                        f.delete();
                    }
                }
            }

        }
    }

    private void banner() {
        try {
            System.out.println();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("banner.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            bf.close();
            System.out.println();
        } catch (Exception e) {
            // not do
        }
    }

    private void treeProject() {
        try {
            Process p = Runtime.getRuntime().exec("tree " + this.projectPath + File.separator + this.applicationName);
            InputStream fis = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            // not do
        }
    }
}


