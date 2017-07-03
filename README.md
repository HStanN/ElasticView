# ElasticView
仿QQ可拖动的小红点效果
# 效果图
![demo](https://github.com/HStanN/ElasticView/blob/master/demo.gif) 

# 属性参数

  color 颜色  
  textColor 字体颜色  
  maxRadius 小圆点（最大）半径  
  minRadius 拖动后小圆点的最小半径  
  maxLength 小圆点的最大可拖动距离  
  count 显示的数字（如果超过99，则显示99+）  

# 如何导入
Maven
```xml
<dependency>
  <groupId>com.hstan.view</groupId>
  <artifactId>elasticlibrary</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

Gradle
```xml
compile 'com.hstan.view:elasticlibrary:1.0.0'
```
  
# 使用注意点
  如果要在实际项目中应用，要使控件可以拖出根布局，需要在根布局中添加属性
  ```xml
  android:clipChildren="false"
  ```
  
  
