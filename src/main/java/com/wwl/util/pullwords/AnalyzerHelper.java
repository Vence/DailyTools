package com.wwl.util.pullwords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * Analyzer帮助类
 * 提供三种方法：
 * 1 、 利用IKanalyzer进行分词，不过这里把姓名也是切分成单字，所以需要建立一个词库，这样才能准确进行分词
 * 2 、 利用百度提供的API，这里分词效果较好，并且提供了相关度等数据
 * 3、  利用IKAnalyzer并且配合扩展以及排除使得分词更加精确
 * 
 * @author 王文路
 * @date 2015-7-14
 */
public class AnalyzerHelper {
	
	private final static String ANALYZER_SEPARATOR = ",";
	
	/**
	 * 阈值
	 */
	public static double YUZHI = 0.2 ;
	
	/**
	 * 分词
	 * 
	 * @author 王文路
	 * @date 2015-7-14
	 * @param sen
	 * @return
	 * @throws IOException
	 */
	public static String[] participle( String sen ){
		
		String res = "";
		
		// 1 创建Analyzer对象

		StringReader reader = new StringReader( sen );
		IKSegmenter ik = new IKSegmenter(reader, true);// 当为true时，分词器进行最大词长切分
		Lexeme lexeme = null;

        // 2 遍历分词结果
        try {
			while((lexeme = ik.next()) != null){  
				
				// 过滤无用分词，如 的 是 等
				if (lexeme.getLexemeText().length() < 2) {
					continue;
				}
			    if( res=="" ) {
			    	res = lexeme.getLexemeText();
			    }
			    else {
			    	res = res + ANALYZER_SEPARATOR + lexeme.getLexemeText();
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        
        reader.close(); 
        return res.split(ANALYZER_SEPARATOR);
	}
	
	/**
	 * 相似度
	 * 
	 * @author 王文路
	 * @date 2015-7-15
	 * @param T1
	 * @param T2
	 * @return
	 * @throws Exception
	 */
	public static double getSimilarity(Vector<String> T1, Vector<String> T2) throws Exception {
		int size = 0 , size2 = 0 ;
	    if ( T1 != null && ( size = T1.size() ) > 0 && T2 != null && ( size2 = T2.size() ) > 0 ) {
	        
	    	Map<String, double[]> T = new HashMap<String, double[]>();
	        
	        //T1和T2的并集T
	    	String index = null ;
	        for ( int i = 0 ; i < size ; i++ ) {
	        	index = T1.get(i) ;
	            if( index != null){
	            	double[] c = T.get(index);
	                c = new double[2];
	                c[0] = 1;	//T1的语义分数Ci
	                c[1] = YUZHI;//T2的语义分数Ci
	                T.put( index, c );
	            }
	        }
	 
	        for ( int i = 0; i < size2 ; i++ ) {
	        	index = T2.get(i) ;
	        	if( index != null ){
	        		double[] c = T.get( index );
	        		if( c != null && c.length == 2 ){
	        			c[1] = 1; //T2中也存在，T2的语义分数=1
	                }else {
	                    c = new double[2];
	                    c[0] = YUZHI; //T1的语义分数Ci
	                    c[1] = 1; //T2的语义分数Ci
	                    T.put( index , c );
	                }
	            }
	        }
	            
	        //开始计算，百分比
	        Iterator<String> it = T.keySet().iterator();
	        double s1 = 0 , s2 = 0, Ssum = 0;  //S1、S2
	        while( it.hasNext() ){
	        	double[] c = T.get( it.next() );
	        	Ssum += c[0]*c[1];
	        	s1 += c[0]*c[0];
	        	s2 += c[1]*c[1];
	        }
	        //百分比
	        return Ssum / Math.sqrt( s1*s2 );
	    } else {
	        throw new Exception("传入参数有问题！");
	    }
	}
	
	/*****************************访问百度提供的API进行分词***********************************/
	/**
	 * 
	 * @author 王文路
	 * @date 2015-7-21
	 * @param httpUrl :请求接口
	 * @param httpArg  :参数
	 * @return
	 */
	public static String[] request(String httpUrl, String httpArg) {
		
	    BufferedReader reader = null;
	    String[] rets = null;
	    
	    StringBuffer sbf = new StringBuffer();
	    httpUrl = httpUrl + "?" + httpArg;

	    try {
	        URL url = new URL(httpUrl);
	        HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
	        connection.setRequestMethod("GET");
	      
	        // 填入apikey到HTTP header
	        connection.setRequestProperty("apikey",  "07b9e5e1a45235358b1b39f26afa2dcd");
	        connection.connect();
	        
	        InputStream is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	        String strRead = null;
	       
	        // 遍历结果集，并且按照相关度由高到低进行排序
	        Map<Double , String> map = new HashMap<Double , String>();
	        List<Double> keys = new ArrayList<Double>();
	        while ((strRead = reader.readLine()) != null) {

	        	String[] ss = strRead.split(":");
	        	
	        	if (ss.length < 2) continue;
	        	
	        	map.put(Double.parseDouble(ss[1]), ss[0]);
	        	keys.add(Double.parseDouble(ss[1]));
	        	
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        
	        Collections.sort(keys , new Comparator<Double>() {

				public int compare(Double o1, Double o2) {
					
					return - o1.compareTo(o2);
				}});
	        
	        rets = new String[keys.size()];
	        
	        for (int i = 0 ; i < keys.size() ; i ++) {
	        	rets[i] = map.get(keys.get(i));
	        }
	        
	        // gc回收
	        map = null;
	        keys = null;
	        
	        reader.close();
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return rets;
	}
	
	/**
	 * 
	 * @author 王文路
	 * @date 2015-7-21
	 * @param words :需要分词的语句
	 * @return 返回分词结果
	 * @throws UnsupportedEncodingException
	 */
	public static String[] pullword(String words) throws UnsupportedEncodingException{
		
		// 进行编码
		words = URLEncoder.encode(words,"UTF-8");
		
		String httpUrl = "http://apis.baidu.com/apistore/pullword/words";
		String httpArg = "source="+words+"&param1=0&param2=1";
		
		return  request(httpUrl, httpArg);
	}
	
	/**********第三种方法，利用扩展和排除分词**************/
	private static String getRequest( String sen ) {
		
		// 尚未初始化，因为第一次执行分词的时候才会初始化，为了在执行分此前手动添加额外的字典，需要先手动的初始化一下
		Dictionary.initial(DefaultConfig.getInstance());
		
		String res = "";
		
		//创建Analyzer对象
		Analyzer an = new IKAnalyzer(true);//智能切词，false表示细颗粒切词
		StringReader reader = new StringReader(sen);
		
		//start 分词
		TokenStream ts=null;
		try {
			ts = an.tokenStream("", reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);  
       
        //遍历分词result
        try {
			while(ts.incrementToken()){  
				
				//去除长度为1的词语
				if( term.toString().length()<=1 ){
//					continue;
				}
			    if( res=="" ) {
			    	res = term.toString();
			    }
			    else {
			    	res = res + ANALYZER_SEPARATOR + term.toString();
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        reader.close(); 
        return res;
	}
	
	public static String[] getPullWords3(String text){
		
		String ret = getRequest(text);
		
		return ret.split(ANALYZER_SEPARATOR);
		
	}


	public static void main(String[] args) throws UnsupportedEncodingException {
		String[] rets = pullword("王文路，北京的学校都很好吗");
		
		for (String ret : rets) {
			System.out.println(ret);
		}

		String[] rets1 = participle("王文路");
		for (String ret : rets1) {
			System.out.println(ret);
		}
	}
	

}
