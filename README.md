# DailyTools
日常工具类，提供如下功能：

##将内容导出html

 - 这一部分没有难点，主要讲利用导出的html转成word内容，这里需要注意的是在导出到html的时候，一要注意中文编码问题，
另外注意对字体字号等进行声明：
WORD中对字体的大小同时采用了两种不同的度量单位，其一是我们中国人所熟悉的“字号”，另外一种则是以“磅”为度量单位。
这两种度量字大小的单位之间是什么样的关系呢？下面就是二者的对应关系： 
<table><thead><tr>
<th>磅</th> <th>字号</th> <tr></thead>
<tbody>
<tr><td>42</td> <td>初号</td> </tr>
<tr><td>36</td> <td>小初 </td> </tr>
<tr><td>26</td> <td>一号 </td> </tr>
<tr><td>24</td> <td>小一号 </td> </tr>
<tr><td>22</td> <td>二号 </td> </tr>
<tr><td>18</td> <td>小二号 </td> </tr>
<tr><td>16</td> <td>三号 </td> </tr>
<tr><td>15</td> <td>小三号 </td> </tr>
<tr><td>14</td> <td>四号 </td> </tr>
<tr><td>12</td> <td>小四号 </td> </tr>
<tr><td>10.5</td> <td>五号 </td> </tr>
<tr><td>9</td> <td>小五号 </td> </tr>
<tr><td>7.5</td> <td>六号 </td> </tr>
<tr><td>6.5</td> <td>小六号 </td> </tr>
<tr><td>5.5</td> <td>七号 </td> </tr>
<tr><td>5</td> <td>八号 </td> </tr>
</tbody></table>

在html中`pt` 代表磅的单位。

- 另外一点需要注意，如果我的html中包含有图片，这个图片地址如果是网站外链，不需要做处理；如果是带有防盗链的图片地址
，还需要将图片的防盗链有效化，也就是要将图片中防盗链信息更换成有效的，比如我的防盗链中包含了`sessionid`  , 
由于有效期只是在会话期间，所以这里需要需要把`sessionid`换成当前用户的有效`sessionid` .

##利用POI框架将html转成word

- poi提供了将内容转成word的语法：

        POIFSFileSystem poifs = new POIFSFileSystem();
			DirectoryEntry directory = poifs.getRoot();
			directory.createDocument(
					"WordDocument", is);

			fos = new FileOutputStream(this.outputPath);
			poifs.writeFilesystem(fos);
			
 
- 那么这里内容既然直接就可以转成html，为什么还要先转成html呢？

**这里是避免转成word的时候失真，而且在控制字体大小颜色，表格样式，图片大小和位置，直接通过POI进行更改格式比较麻烦。**
**这里就是先把内容，布局，样式等做好，转成html，然后利用POI将html转成word的时候是转成web大纲样式的，所以失真较少。**
 
##分词功能
这里提供了分词的三种方法

 - 一种是利用IKAnalyzer2012FF_u1.jar包，这个包是针对于Lucene4.0以上的，如果低版本请使用IKAnalyzer2012.jar；
 两者在包的方法上都有差别。
 
 这种分词的效果需要借助分词库才能精确分词，所以这里的工作量就转换成要建立一套合理的词库。
 否则的话，这种方法对于专有名词分词效果很差，最差的情况下分成的全是单字，比如我搜索人名“王文路” ， 
 如果不加任何分词库的话，分出来的结果是 ： “王” 、 “文” 、“路” ，这显示不是我想要的结果，
 如果我分词以后是拿着分词结果做全局检索的话，我只想搜索出包含“王文路”的数据，结果将包含“王”、“文”、“路”的内容
 全部都搜索出来了，这显然比我理想中的结果集要大很多，这里的难点就是和进行实时的补充词库、本体等；
 
 - 另外一种是调用[百度的APIStore](http://apistore.baidu.com/) 的API接口，百度实现的分词效果不错，就算连人名也会精确搜索出，
 并且给出每一个分词结果的相关度
 
 ![](http://i3.piimg.com/35a4bad53683c0ef.png)
 
  ![](http://i3.piimg.com/b0e2c20276bbc267.png)
  
  百度还提供了其他很多种API，可以通过一样的方法进行访问。

 - 另外一种方法也是利用IKAnalyzer进行分词的，不同的是添加了配置文件，配置`ext_dict` 和 `ext_stopwords` , 需要添加配置文件和词典文件，这里需要注意的是`IKAnalyzer.cfg.xml`配置文件只能放在src根目录下，而词典也就是dic文件可以放在任何位置，需要在IKAnalyzer.cfg.xml配置文件中声明路径。
 
对于以上路径问题，如果是maven项目，并且词典文件也是放在src下，编译的时候并不会把src文件编译到target下，所以需要在`pom.xml`
中使src进入build目录

		<build>
		<resources>
			<resource>
				<directory>src/main/resources</directory>
				<includes>
					<include>**/*.dic</include>
				</includes>
				<filtering>true</filtering>
			</resource>
		</resources>
		</build>

