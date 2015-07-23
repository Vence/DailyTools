package com.wwl.util.html2word;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 将内容导出到html,避免图片失真
 * 
 * 需要html将图片中的防盗链有效化，这样导出的word中也会包含有效图片
 * 
 * @author 王文路
 * @date 2015-7-22
 */
public class Content2HtmlHelper {
	
	
	/**
	 *  要转换的内容
	 */
	
	private String content; 
	/**
	 * 转换后生成的html保存文件路径，以.html结尾
	 */
	
	private String filePath ;
	
	/**
	 * 如： http://localhost/test/api/{sessionid}，用于解除图片防盗链
	 * 因为sessionid每次登陆都不一样，如果我的content中包含本系统带有防盗链的图片，需要处理一下
	 */
	private String basePath;	

	public Content2HtmlHelper(String content , String filePath ,String basePath) {
		super();
		this.content = content;
		this.filePath = filePath;
		this.basePath = basePath;
	}

	private boolean import2Html(){
		
		StringBuffer buf = new StringBuffer();
		
		// 1 添加html文件头，注意编码方式utf-8
		buf.append("<!DOCTYPE html><html><head>")
		.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">")
		.append("</head><body>");
		
		// 2 添加头文字，固定，字体对应到word中，16磅对应word中三号字体
		buf.append("<p style='font-size:16.0pt;font-family:黑体'>测试文件</p>");
		
		// 3 将图片的防盗链有效化
		org.jsoup.nodes.Document doc = Jsoup.parse(this.content);  
		Elements els = doc.getElementsByTag("img");
		
		// 3.1 解析图片地址
		Iterator<Element> it = els.iterator();
		while(it.hasNext()) {
			
			Element el = it.next();
			
			String src = el.attr("src");
			
			el.attr("src", this.basePath + src.substring(36));
		}
		
		// 3.2 解析后， 取出body内容(由于Jsoup会自动加上html和head等标签，我们只需要body的内容)
		Elements head =  doc.getElementsByTag("body");
		buf.append( head.iterator().next().html() );
		
		// 4 
		buf.append("</body></html>");
		
		FileOutputStream fos = null;   
		BufferedWriter bw = null;  
		 
		try {
			
			// 5 写入html文件
			File file = new File(this.filePath);
			fos = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			bw.write(buf.toString());
			
			System.out.println("将信息点转换html文件完成");
			
			return true;
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}catch (IOException ioe) { 
			
            ioe.printStackTrace();  
            
        } finally {  
            try {  
                if (bw != null)  
                    bw.close();  
                if (fos != null)  
                    fos.close();  
            } catch (IOException ie) {  
            }  
        }  
		
		return false;
		
	}
	
	/**
	 * 单元测试
	 * 
	 * @author 王文路
	 * @date 2015-7-23
	 * @param args
	 */
	public static void main(String[] args){
		
		new Content2HtmlHelper("测试" , "G:\\123.html" , null).import2Html();
	}

}

